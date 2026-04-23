package com.ordertracking.orderservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank String customerId,
    @NotEmpty List<OrderItemRequest> items
) {
    public record OrderItemRequest(
        @NotBlank String productId,
        int quantity
    ) {}
}
