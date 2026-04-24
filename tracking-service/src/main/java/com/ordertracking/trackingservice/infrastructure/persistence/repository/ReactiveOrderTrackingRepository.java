package com.ordertracking.trackingservice.infrastructure.persistence.repository;

import com.ordertracking.trackingservice.infrastructure.persistence.document.OrderTrackingDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveOrderTrackingRepository extends ReactiveMongoRepository<OrderTrackingDocument, String> {
    Mono<OrderTrackingDocument> findByOrderId(String orderId);
}
