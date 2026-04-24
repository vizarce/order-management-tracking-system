package com.ordertracking.trackingservice.infrastructure.persistence.adapter;

import com.ordertracking.trackingservice.domain.model.OrderTracking;
import com.ordertracking.trackingservice.domain.repository.OrderTrackingRepository;
import com.ordertracking.trackingservice.infrastructure.persistence.mapper.OrderTrackingMapper;
import com.ordertracking.trackingservice.infrastructure.persistence.repository.ReactiveOrderTrackingRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrderTrackingRepositoryAdapter implements OrderTrackingRepository {
    private final ReactiveOrderTrackingRepository repository;
    private final OrderTrackingMapper mapper;

    public OrderTrackingRepositoryAdapter(ReactiveOrderTrackingRepository repository, OrderTrackingMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<OrderTracking> save(OrderTracking tracking) {
        return repository.save(mapper.toDocument(tracking)).map(mapper::toDomain);
    }

    @Override
    public Mono<OrderTracking> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId).map(mapper::toDomain);
    }
}
