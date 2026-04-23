package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.dto.CreateOrderResponse;
import com.ordertracking.orderservice.application.dto.OrderResponse;
import com.ordertracking.orderservice.application.usecase.CreateOrderUseCase;
import com.ordertracking.orderservice.application.usecase.GetOrderUseCase;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.OrderStatus;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import com.ordertracking.orderservice.domain.repository.OrderRepository;
import com.ordertracking.orderservice.infrastructure.kafka.producer.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderApplicationService {
    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderApplicationService(CreateOrderUseCase createOrderUseCase, GetOrderUseCase getOrderUseCase,
                                   OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = createOrderUseCase.execute(request);
        orderEventProducer.publishOrderCreated(order);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);
                log.info("Simulating payment processing for order {}", order.getId());
                Order managed = orderRepository.findById(order.getId()).orElse(null);
                if (managed != null) {
                    managed.confirm();
                    orderRepository.save(managed);
                    log.info("Order {} confirmed (PROCESSING)", order.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Async processing interrupted for order {}", order.getId());
            } catch (Exception e) {
                log.error("Async processing failed for order {}", order.getId(), e);
            }
        });

        return CreateOrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = getOrderUseCase.execute(orderId);
        return OrderResponse.from(order);
    }
}
