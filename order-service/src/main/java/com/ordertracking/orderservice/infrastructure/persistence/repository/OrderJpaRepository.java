package com.ordertracking.orderservice.infrastructure.persistence.repository;

import com.ordertracking.orderservice.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    /** Eagerly fetches order items in a single JOIN to avoid N+1 queries. */
    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findByCustomerId(UUID customerId);

    /** Eagerly fetches order items when looking up a single order. */
    @EntityGraph(attributePaths = "items")
    @Override
    Optional<OrderEntity> findById(UUID id);

    /** Eagerly fetches order items for all orders to avoid N+1 queries. */
    @EntityGraph(attributePaths = "items")
    @Override
    List<OrderEntity> findAll();
}
