package com.ordertracking.orderservice.application.service;

import com.ordertracking.common.mdc.MdcConstants;
import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.dto.CreateOrderResponse;
import com.ordertracking.orderservice.application.dto.OrderResponse;
import com.ordertracking.orderservice.application.usecase.CreateOrderUseCase;
import com.ordertracking.orderservice.application.usecase.GetOrderUseCase;
import com.ordertracking.orderservice.domain.exception.OrderNotFoundException;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.OrderItem;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import com.ordertracking.orderservice.infrastructure.kafka.producer.OrderEventProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock private CreateOrderUseCase createOrderUseCase;
    @Mock private GetOrderUseCase getOrderUseCase;
    @Mock private OrderEventProducer orderEventProducer;
    @Mock private OrderConfirmationHelper orderConfirmationHelper;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
            createOrderUseCase, getOrderUseCase, orderEventProducer, orderConfirmationHelper);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
        MDC.clear();
    }

    @Test
    void createOrder_delegatesToUseCaseAndReturnsResponse() {
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        Order order = Order.create(customerId);
        order.addItem(new OrderItem("prod-1", "Widget", 2, Money.of(BigDecimal.TEN, "USD")));

        var request = new CreateOrderRequest(customerId.toString(),
            List.of(new CreateOrderRequest.OrderItemRequest("prod-1", 2)));

        when(createOrderUseCase.execute(request)).thenReturn(order);

        CreateOrderResponse response = service.createOrder(request);

        assertThat(response.orderId()).isEqualTo(order.getId().toString());
        assertThat(response.customerId()).isEqualTo(customerId.toString());
        assertThat(response.status()).isEqualTo("PENDING");
        verify(orderEventProducer).publishOrderCreated(order);
    }

    @Test
    void getOrder_delegatesToUseCaseAndReturnsResponse() {
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        Order order = Order.create(customerId);
        String orderId = order.getId().toString();

        when(getOrderUseCase.execute(orderId)).thenReturn(order);

        OrderResponse response = service.getOrder(orderId);

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void getOrder_propagatesExceptionWhenOrderNotFound() {
        String orderId = UUID.randomUUID().toString();
        when(getOrderUseCase.execute(orderId)).thenThrow(new OrderNotFoundException(orderId));

        assertThatThrownBy(() -> service.getOrder(orderId))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void createOrder_propagatesMdcContextToAsyncThread() throws InterruptedException {
        MDC.put(MdcConstants.TRACE_ID,   "async-trace-id");
        MDC.put(MdcConstants.REQUEST_ID, "async-request-id");
        MDC.put(MdcConstants.USER_ID,    "async-user-id");

        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        Order order = Order.create(customerId);
        order.addItem(new OrderItem("prod-1", "Widget", 1, Money.of(BigDecimal.TEN, "USD")));

        var request = new CreateOrderRequest(customerId.toString(),
            List.of(new CreateOrderRequest.OrderItemRequest("prod-1", 1)));

        when(createOrderUseCase.execute(request)).thenReturn(order);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> asyncTrace   = new AtomicReference<>();
        AtomicReference<String> asyncRequest = new AtomicReference<>();

        doAnswer(inv -> {
            asyncTrace.set(MDC.get(MdcConstants.TRACE_ID));
            asyncRequest.set(MDC.get(MdcConstants.REQUEST_ID));
            latch.countDown();
            return null;
        }).when(orderConfirmationHelper).confirmOrder(any(UUID.class));

        service.createOrder(request);

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(asyncTrace.get()).isEqualTo("async-trace-id");
        assertThat(asyncRequest.get()).isEqualTo("async-request-id");
    }
}

