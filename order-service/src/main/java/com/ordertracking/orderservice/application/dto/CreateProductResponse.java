package com.ordertracking.orderservice.application.dto;

import com.ordertracking.orderservice.domain.model.Product;
import java.math.BigDecimal;

public record CreateProductResponse(
    String id,
    String name,
    String description,
    BigDecimal price,
    String currency,
    int stockQuantity
) {
    public static CreateProductResponse from(Product product) {
        return new CreateProductResponse(
            product.getId(), product.getName(), product.getDescription(),
            product.getPrice().amount(), product.getPrice().currency(),
            product.getStockQuantity()
        );
    }
}
