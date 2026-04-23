package com.ordertracking.orderservice.infrastructure.persistence.mapper;

import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice().amount());
        entity.setCurrency(product.getPrice().currency());
        entity.setStockQuantity(product.getStockQuantity());
        return entity;
    }

    public Product toDomain(ProductEntity entity) {
        Product product = new Product();
        product.setId(entity.getId());
        product.setName(entity.getName());
        product.setDescription(entity.getDescription());
        product.setPrice(Money.of(entity.getPrice(), entity.getCurrency()));
        product.setStockQuantity(entity.getStockQuantity());
        return product;
    }
}
