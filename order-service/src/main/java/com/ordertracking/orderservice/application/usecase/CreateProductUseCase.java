package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.domain.model.Product;

public interface CreateProductUseCase {
    Product execute(CreateProductRequest request);
}
