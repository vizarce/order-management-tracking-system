package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.domain.exception.DomainException;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import com.ordertracking.orderservice.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class GetOrderUseCase {
    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order execute(String orderId) {
        return orderRepository.findById(OrderId.of(orderId))
            .orElseThrow(() -> new DomainException("Order not found with id: " + orderId));
    }
}
