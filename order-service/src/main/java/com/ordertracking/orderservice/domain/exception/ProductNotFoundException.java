package com.ordertracking.orderservice.domain.exception;

public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(String id) {
        super("Product not found with id: " + id);
    }
}
