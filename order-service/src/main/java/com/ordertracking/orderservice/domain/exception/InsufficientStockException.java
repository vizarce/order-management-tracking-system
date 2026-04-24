package com.ordertracking.orderservice.domain.exception;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format("Insufficient stock for product %s: requested=%d, available=%d", productId, requested, available));
    }
}
