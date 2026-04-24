package com.ordertracking.common.event;

import com.ordertracking.common.dto.OrderItemDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedEventTest {

    @Test
    void shouldCreateEventWithAllFields() {
        Instant now = Instant.now();
        List<OrderItemDto> items = List.of(new OrderItemDto("p1", "Widget", 2, BigDecimal.valueOf(9.99)));

        OrderCreatedEvent event = new OrderCreatedEvent(
            "order-1", "customer-1", "PENDING",
            BigDecimal.valueOf(19.98), items, now, "trace-abc", "req-xyz"
        );

        assertThat(event.orderId()).isEqualTo("order-1");
        assertThat(event.customerId()).isEqualTo("customer-1");
        assertThat(event.status()).isEqualTo("PENDING");
        assertThat(event.totalAmount()).isEqualByComparingTo("19.98");
        assertThat(event.items()).hasSize(1);
        assertThat(event.occurredAt()).isEqualTo(now);
        assertThat(event.traceId()).isEqualTo("trace-abc");
        assertThat(event.requestId()).isEqualTo("req-xyz");
    }

    @Test
    void shouldSupportNullableOptionalFields() {
        OrderCreatedEvent event = new OrderCreatedEvent(
            "order-2", "customer-2", "PENDING",
            BigDecimal.ZERO, null, null, null, null
        );

        assertThat(event.items()).isNull();
        assertThat(event.occurredAt()).isNull();
        assertThat(event.traceId()).isNull();
        assertThat(event.requestId()).isNull();
    }

    @Test
    void shouldImplementValueEquality() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        List<OrderItemDto> items = List.of(new OrderItemDto("p1", "Widget", 1, BigDecimal.TEN));

        OrderCreatedEvent a = new OrderCreatedEvent("o1", "c1", "PENDING", BigDecimal.TEN, items, now, "t1", "r1");
        OrderCreatedEvent b = new OrderCreatedEvent("o1", "c1", "PENDING", BigDecimal.TEN, items, now, "t1", "r1");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
