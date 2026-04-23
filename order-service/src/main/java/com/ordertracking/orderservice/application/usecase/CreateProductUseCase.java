package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class CreateProductUseCase {
    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(CreateProductRequest request) {
        String currency = request.currency() != null ? request.currency() : "USD";
        Product product = Product.create(request.name(), request.description(), Money.of(request.price(), currency), request.stockQuantity());
        return productRepository.save(product);
    }
}
