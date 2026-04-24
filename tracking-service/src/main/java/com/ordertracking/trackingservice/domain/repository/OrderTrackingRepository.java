package com.ordertracking.trackingservice.domain.repository;

import com.ordertracking.trackingservice.domain.model.OrderTracking;
import reactor.core.publisher.Mono;

public interface OrderTrackingRepository {
    Mono<OrderTracking> save(OrderTracking tracking);
    Mono<OrderTracking> findByOrderId(String orderId);
}
