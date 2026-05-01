package com.ordertracking.trackingservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock OrderTrackingService orderTrackingService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    OrderEventConsumer consumer;

    @BeforeEach
    void setup() {
        consumer = new OrderEventConsumer(objectMapper, orderTrackingService);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ConsumerRecord<String, String> record(String payload) {
        return record(payload, null, null);
    }

    private ConsumerRecord<String, String> record(String payload, String traceId, String requestId) {
        RecordHeaders headers = new RecordHeaders();
        if (traceId   != null) headers.add("X-Trace-Id",   traceId.getBytes(StandardCharsets.UTF_8));
        if (requestId != null) headers.add("X-Request-Id", requestId.getBytes(StandardCharsets.UTF_8));
        return new ConsumerRecord<>("orders.events", 0, 0L, -1L, TimestampType.NO_TIMESTAMP_TYPE,
            -1, -1, "key", payload, headers, Optional.empty());
    }

    private OrderTrackingDto sampleDto(String orderId, String status) {
        return new OrderTrackingDto(orderId, "cust-1", status,
            BigDecimal.TEN, Collections.emptyList(), Collections.emptyList(), Instant.now(), Instant.now());
    }

    // ── OrderCreatedEvent tests ────────────────────────────────────────────────

    @Test
    void shouldCreateTrackingForNewOrder() throws Exception {
        String payload = objectMapper.writeValueAsString(new Object() {
            public String orderId      = "order-1";
            public String customerId   = "cust-1";
            public String status       = "PENDING";
            public BigDecimal totalAmount = BigDecimal.TEN;
            public Object[] items      = {};
            public Instant occurredAt  = Instant.now();
            public Object traceId      = null;
            public Object requestId    = null;
        });

        when(orderTrackingService.getTracking("order-1")).thenReturn(Mono.empty());
        when(orderTrackingService.saveTracking(any())).thenReturn(Mono.just(sampleDto("order-1", "PENDING")));

        consumer.consume(record(payload));

        verify(orderTrackingService).saveTracking(any());
    }

    @Test
    void shouldSkipDuplicateOrderCreatedEvent() throws Exception {
        String payload = objectMapper.writeValueAsString(new Object() {
            public String orderId      = "order-1";
            public String customerId   = "cust-1";
            public String status       = "PENDING";
            public BigDecimal totalAmount = BigDecimal.TEN;
            public Object[] items      = {};
            public Instant occurredAt  = Instant.now();
            public Object traceId      = null;
            public Object requestId    = null;
        });

        when(orderTrackingService.getTracking("order-1"))
            .thenReturn(Mono.just(sampleDto("order-1", "PENDING")));

        consumer.consume(record(payload));

        verify(orderTrackingService, never()).saveTracking(any());
    }

    // ── OrderStatusUpdatedEvent tests ──────────────────────────────────────────

    @Test
    void shouldUpdateTrackingStatusOnStatusUpdatedEvent() throws Exception {
        String payload = objectMapper.writeValueAsString(new Object() {
            public String orderId        = "order-2";
            public String previousStatus = "PENDING";
            public String newStatus      = "SHIPPED";
            public Instant occurredAt    = Instant.now();
            public Object traceId        = null;
            public Object requestId      = null;
        });

        when(orderTrackingService.updateTrackingStatus("order-2", "SHIPPED"))
            .thenReturn(Mono.just(sampleDto("order-2", "SHIPPED")));

        consumer.consume(record(payload));

        verify(orderTrackingService).updateTrackingStatus("order-2", "SHIPPED");
        verify(orderTrackingService, never()).saveTracking(any());
    }

    @Test
    void shouldHandleMissingTrackingOnStatusUpdate() throws Exception {
        String payload = objectMapper.writeValueAsString(new Object() {
            public String orderId        = "order-missing";
            public String previousStatus = "PENDING";
            public String newStatus      = "SHIPPED";
            public Instant occurredAt    = Instant.now();
            public Object traceId        = null;
            public Object requestId      = null;
        });

        when(orderTrackingService.updateTrackingStatus("order-missing", "SHIPPED"))
            .thenReturn(Mono.empty());

        consumer.consume(record(payload));

        verify(orderTrackingService).updateTrackingStatus("order-missing", "SHIPPED");
    }

    // ── MDC / header restoration tests ────────────────────────────────────────

    @Test
    void shouldRestoreMdcFromHeaders() throws Exception {
        String payload = objectMapper.writeValueAsString(new Object() {
            public String orderId      = "order-3";
            public String customerId   = "cust-3";
            public String status       = "PENDING";
            public BigDecimal totalAmount = BigDecimal.ONE;
            public Object[] items      = {};
            public Instant occurredAt  = Instant.now();
            public Object traceId      = null;
            public Object requestId    = null;
        });

        when(orderTrackingService.getTracking("order-3")).thenReturn(Mono.empty());
        when(orderTrackingService.saveTracking(any())).thenReturn(Mono.just(sampleDto("order-3", "PENDING")));

        // Should not throw when trace/request headers are present
        consumer.consume(record(payload, "trace-abc", "req-xyz"));

        verify(orderTrackingService).saveTracking(any());
    }

    @Test
    void shouldHandleMalformedPayloadGracefully() {
        // A completely invalid payload must not propagate an exception — the consumer
        // logs the error and returns so that the Kafka offset is still committed.
        consumer.consume(record("not-valid-json"));
        verifyNoInteractions(orderTrackingService);
    }
}
