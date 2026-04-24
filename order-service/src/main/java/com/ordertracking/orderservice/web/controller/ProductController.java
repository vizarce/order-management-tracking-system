package com.ordertracking.orderservice.web.controller;

import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.application.dto.CreateProductResponse;
import com.ordertracking.orderservice.application.service.ProductApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productApplicationService.createProduct(request);
    }

    @GetMapping("/{id}")
    public CreateProductResponse getProduct(@PathVariable String id) {
        return productApplicationService.getProduct(id);
    }

    @GetMapping
    public List<CreateProductResponse> getAllProducts() {
        return productApplicationService.getAllProducts();
    }
}
