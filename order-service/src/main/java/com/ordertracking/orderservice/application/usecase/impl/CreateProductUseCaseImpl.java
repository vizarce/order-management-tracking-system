package com.ordertracking.orderservice.application.usecase.impl;

import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.application.usecase.CreateProductUseCase;
import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class CreateProductUseCaseImpl implements CreateProductUseCase {
    private final ProductRepository productRepository;

    public CreateProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product execute(CreateProductRequest request) {
        String currency = request.currency() != null ? request.currency() : "USD";
        Product product = Product.create(request.name(), request.description(),
            Money.of(request.price(), currency), request.stockQuantity());
        return productRepository.save(product);
    }
}
