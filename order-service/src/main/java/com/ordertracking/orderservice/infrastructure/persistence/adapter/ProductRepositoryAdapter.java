package com.ordertracking.orderservice.infrastructure.persistence.adapter;

import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.repository.ProductRepository;
import com.ordertracking.orderservice.infrastructure.persistence.mapper.ProductMapper;
import com.ordertracking.orderservice.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository, ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
