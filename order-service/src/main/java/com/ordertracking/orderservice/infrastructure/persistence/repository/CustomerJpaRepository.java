package com.ordertracking.orderservice.infrastructure.persistence.repository;

import com.ordertracking.orderservice.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
    boolean existsByEmail(String email);
}
