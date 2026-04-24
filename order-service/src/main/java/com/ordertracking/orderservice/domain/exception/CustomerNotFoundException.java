package com.ordertracking.orderservice.domain.exception;

public class CustomerNotFoundException extends DomainException {
    public CustomerNotFoundException(String id) {
        super("Customer not found with id: " + id);
    }
}
