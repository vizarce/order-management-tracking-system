package com.ordertracking.orderservice.domain.exception;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(String id) {
        super("Order not found with id: " + id);
    }
}
