package com.ordertracking.common.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusUpdatedEventTest {

    @Test
    void shouldCreateEventWithAllFields() {
        Instant now = Instant.now();

        OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
            "order-1", "PENDING", "PROCESSING", now, "trace-abc", "req-xyz"
        );

        assertThat(event.orderId()).isEqualTo("order-1");
        assertThat(event.previousStatus()).isEqualTo("PENDING");
        assertThat(event.newStatus()).isEqualTo("PROCESSING");
        assertThat(event.occurredAt()).isEqualTo(now);
        assertThat(event.traceId()).isEqualTo("trace-abc");
        assertThat(event.requestId()).isEqualTo("req-xyz");
    }

    @Test
    void shouldSupportNullableOptionalFields() {
        OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
            "order-2", "PENDING", "CANCELLED", null, null, null
        );

        assertThat(event.occurredAt()).isNull();
        assertThat(event.traceId()).isNull();
        assertThat(event.requestId()).isNull();
    }

    @Test
    void shouldImplementValueEquality() {
        Instant now = Instant.parse("2024-06-01T12:00:00Z");

        OrderStatusUpdatedEvent a = new OrderStatusUpdatedEvent("o1", "PENDING", "SHIPPED", now, "t1", "r1");
        OrderStatusUpdatedEvent b = new OrderStatusUpdatedEvent("o1", "PENDING", "SHIPPED", now, "t1", "r1");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
