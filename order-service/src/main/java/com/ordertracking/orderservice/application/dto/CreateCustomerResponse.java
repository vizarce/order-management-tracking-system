package com.ordertracking.orderservice.application.dto;

import com.ordertracking.orderservice.domain.model.Customer;

public record CreateCustomerResponse(
    String id,
    String name,
    String email
) {
    public static CreateCustomerResponse from(Customer customer) {
        return new CreateCustomerResponse(customer.getId().toString(), customer.getName(), customer.getEmail().value());
    }
}
