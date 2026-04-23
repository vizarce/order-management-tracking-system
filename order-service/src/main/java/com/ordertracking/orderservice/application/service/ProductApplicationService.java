package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.application.dto.CreateProductResponse;
import com.ordertracking.orderservice.application.usecase.CreateProductUseCase;
import com.ordertracking.orderservice.domain.exception.ProductNotFoundException;
import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductApplicationService {
    private final CreateProductUseCase createProductUseCase;
    private final ProductRepository productRepository;

    public ProductApplicationService(CreateProductUseCase createProductUseCase, ProductRepository productRepository) {
        this.createProductUseCase = createProductUseCase;
        this.productRepository = productRepository;
    }

    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        Product product = createProductUseCase.execute(request);
        return CreateProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public CreateProductResponse getProduct(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        return CreateProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<CreateProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(CreateProductResponse::from)
            .collect(Collectors.toList());
    }
}
