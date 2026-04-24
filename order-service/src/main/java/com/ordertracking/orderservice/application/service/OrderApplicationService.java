package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.dto.CreateOrderResponse;
import com.ordertracking.orderservice.application.dto.OrderResponse;
import com.ordertracking.orderservice.application.usecase.CreateOrderUseCase;
import com.ordertracking.orderservice.application.usecase.GetOrderUseCase;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.infrastructure.kafka.producer.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class OrderApplicationService {
    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    /** Dedicated single-thread scheduler for the payment-simulation delay.
     *  Using a ScheduledExecutorService avoids blocking a thread during the wait. */
    private static final ScheduledExecutorService SCHEDULER =
        new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "order-async-scheduler");
            t.setDaemon(true);
            return t;
        });

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OrderEventProducer orderEventProducer;
    /**
     * Separate Spring bean used for the async confirmation step so that
     * Spring's proxy-based AOP is correctly applied and the new transaction
     * from {@code @Transactional(propagation = REQUIRES_NEW)} is honoured.
     */
    private final OrderConfirmationHelper orderConfirmationHelper;

    public OrderApplicationService(CreateOrderUseCase createOrderUseCase,
                                   GetOrderUseCase getOrderUseCase,
                                   OrderEventProducer orderEventProducer,
                                   OrderConfirmationHelper orderConfirmationHelper) {
        this.createOrderUseCase      = createOrderUseCase;
        this.getOrderUseCase         = getOrderUseCase;
        this.orderEventProducer      = orderEventProducer;
        this.orderConfirmationHelper = orderConfirmationHelper;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = createOrderUseCase.execute(request);
        orderEventProducer.publishOrderCreated(order);

        // Capture MDC context before the async boundary so log statements inside
        // the background thread carry the same trace/request correlation IDs.
        final Map<String, String> mdcContext = org.slf4j.MDC.getCopyOfContextMap();
        final java.util.UUID orderId = order.getId().value();

        // Schedule background payment simulation after a 500 ms delay using
        // ScheduledExecutorService so no thread is blocked during the wait.
        CompletableFuture.runAsync(() -> {
            if (mdcContext != null) org.slf4j.MDC.setContextMap(mdcContext);
            try {
                log.info("Simulating payment processing for order {}", orderId);
                // Delegate to a separate Spring bean to ensure AOP proxy is applied
                // and the REQUIRES_NEW transaction is independent of the outer one.
                orderConfirmationHelper.confirmOrder(orderId);
            } catch (Exception e) {
                log.error("Async processing failed for order {}", orderId, e);
            } finally {
                org.slf4j.MDC.clear();
            }
        }, command -> SCHEDULER.schedule(command, 500, TimeUnit.MILLISECONDS));

        return CreateOrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = getOrderUseCase.execute(orderId);
        return OrderResponse.from(order);
    }
}
