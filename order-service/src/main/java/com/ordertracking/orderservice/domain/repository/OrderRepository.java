package com.ordertracking.orderservice.domain.repository;

import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findAll();
}
