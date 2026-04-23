package com.ordertracking.orderservice.domain.event;

import com.ordertracking.orderservice.domain.model.Order;
import java.time.Instant;

public class OrderCreatedEvent {
    private final Order order;
    private final Instant occurredAt;

    public OrderCreatedEvent(Order order) {
        this.order = order;
        this.occurredAt = Instant.now();
    }

    public Order getOrder() { return order; }
    public Instant getOccurredAt() { return occurredAt; }
}
