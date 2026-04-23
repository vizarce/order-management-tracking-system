package com.ordertracking.orderservice.infrastructure.persistence.repository;

import com.ordertracking.orderservice.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {}
