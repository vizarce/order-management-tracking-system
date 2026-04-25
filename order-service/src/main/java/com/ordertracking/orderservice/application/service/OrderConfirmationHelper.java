package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import com.ordertracking.orderservice.domain.repository.OrderRepository;
import com.ordertracking.orderservice.infrastructure.kafka.producer.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Separate Spring-managed bean that executes order confirmation in its own transaction.
 *
 * <p>This class exists specifically to avoid the Spring AOP proxy self-invocation problem.
 * If the confirm logic lived in {@link OrderApplicationService} and was called from within
 * the same bean (self-invocation), Spring's proxy-based AOP would bypass the
 * {@code @Transactional} advice. By delegating to a different bean we ensure the proxy
 * is invoked correctly and the new transaction is truly independent of the outer one.</p>
 */
@Component
public class OrderConfirmationHelper {
    private static final Logger log = LoggerFactory.getLogger(OrderConfirmationHelper.class);

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderConfirmationHelper(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    /**
     * Loads the order, calls {@code confirm()}, and saves — all inside a new, independent transaction.
     * Using {@link Propagation#REQUIRES_NEW} means this always starts a fresh transaction regardless
     * of any outer transaction, which is correct for async background processing.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrder(UUID orderId) {
        orderRepository.findById(OrderId.of(orderId)).ifPresent(order -> {
            String previousStatus = order.getStatus().name();
            order.confirm();
            orderRepository.save(order);
            log.info("Order {} confirmed (PROCESSING)", orderId);
            orderEventProducer.publishOrderStatusUpdated(order, previousStatus);
        });
    }
}
