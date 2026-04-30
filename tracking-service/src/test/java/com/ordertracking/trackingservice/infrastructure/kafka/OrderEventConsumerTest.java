package com.ordertracking.trackingservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordertracking.common.event.OrderCreatedEvent;
import com.ordertracking.common.mdc.MdcConstants;
import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import com.ordertracking.trackingservice.infrastructure.kafka.consumer.OrderEventConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock private OrderTrackingService orderTrackingService;

    private ObjectMapper objectMapper;
    private OrderEventConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumer = new OrderEventConsumer(objectMapper, orderTrackingService);
        MDC.clear();
        // The consumer checks for an existing tracking document before saving (idempotency).
        // Return empty so the not-found path proceeds to saveTracking in all tests here.
        lenient().when(orderTrackingService.getTracking(anyString())).thenReturn(Mono.empty());
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void restoresMdcFromKafkaHeaders() throws Exception {
        OrderTrackingDto savedDto = new OrderTrackingDto(
            "order-1", "cust-1", "PENDING", BigDecimal.TEN, Collections.emptyList(), Instant.now(), Instant.now());

        AtomicReference<String> capturedTrace   = new AtomicReference<>();
        AtomicReference<String> capturedRequest = new AtomicReference<>();
        AtomicReference<String> capturedUser    = new AtomicReference<>();

        String payload = objectMapper.writeValueAsString(buildEvent("order-1"));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            "orders.events", 0, 0L, "order-1", payload);
        record.headers().add(new RecordHeader(MdcConstants.HEADER_TRACE_ID,   "kafka-trace".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(MdcConstants.HEADER_REQUEST_ID, "kafka-req".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader(MdcConstants.HEADER_USER_ID,    "kafka-user".getBytes(StandardCharsets.UTF_8)));

        when(orderTrackingService.saveTracking(any())).thenAnswer(inv -> {
            capturedTrace.set(MDC.get(MdcConstants.TRACE_ID));
            capturedRequest.set(MDC.get(MdcConstants.REQUEST_ID));
            capturedUser.set(MDC.get(MdcConstants.USER_ID));
            return Mono.just(savedDto);
        });

        consumer.consume(record);

        assertThat(capturedTrace.get()).isEqualTo("kafka-trace");
        assertThat(capturedRequest.get()).isEqualTo("kafka-req");
        assertThat(capturedUser.get()).isEqualTo("kafka-user");
    }

    @Test
    void generatesFreshTraceIdWhenHeaderAbsent() throws Exception {
        OrderTrackingDto savedDto = new OrderTrackingDto(
            "order-2", "cust-2", "PENDING", BigDecimal.TEN, Collections.emptyList(), Instant.now(), Instant.now());

        AtomicReference<String> capturedTrace = new AtomicReference<>();
        when(orderTrackingService.saveTracking(any())).thenAnswer(inv -> {
            capturedTrace.set(MDC.get(MdcConstants.TRACE_ID));
            return Mono.just(savedDto);
        });

        String payload = objectMapper.writeValueAsString(buildEvent("order-2"));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            "orders.events", 0, 1L, "order-2", payload);

        consumer.consume(record);

        assertThat(capturedTrace.get()).isNotNull().isNotBlank();
    }

    @Test
    void clearsMdcAfterConsumption() throws Exception {
        MDC.put(MdcConstants.TRACE_ID, "pre-existing-trace");

        OrderTrackingDto savedDto = new OrderTrackingDto(
            "order-3", "cust-3", "PENDING", BigDecimal.TEN, Collections.emptyList(), Instant.now(), Instant.now());
        when(orderTrackingService.saveTracking(any())).thenReturn(Mono.just(savedDto));

        String payload = objectMapper.writeValueAsString(buildEvent("order-3"));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            "orders.events", 0, 2L, "order-3", payload);

        consumer.consume(record);

        assertThat(MDC.get(MdcConstants.TRACE_ID)).isNull();
        assertThat(MDC.get(MdcConstants.REQUEST_ID)).isNull();
    }

    @Test
    void setsKafkaMetadataInMdc() throws Exception {
        OrderTrackingDto savedDto = new OrderTrackingDto(
            "order-4", "cust-4", "PENDING", BigDecimal.TEN, Collections.emptyList(), Instant.now(), Instant.now());

        AtomicReference<String> capturedTopic     = new AtomicReference<>();
        AtomicReference<String> capturedPartition = new AtomicReference<>();
        AtomicReference<String> capturedOffset    = new AtomicReference<>();

        when(orderTrackingService.saveTracking(any())).thenAnswer(inv -> {
            capturedTopic.set(MDC.get(MdcConstants.TOPIC));
            capturedPartition.set(MDC.get(MdcConstants.PARTITION));
            capturedOffset.set(MDC.get(MdcConstants.OFFSET));
            return Mono.just(savedDto);
        });

        String payload = objectMapper.writeValueAsString(buildEvent("order-4"));
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            "orders.events", 2, 42L, "order-4", payload);

        consumer.consume(record);

        assertThat(capturedTopic.get()).isEqualTo("orders.events");
        assertThat(capturedPartition.get()).isEqualTo("2");
        assertThat(capturedOffset.get()).isEqualTo("42");
    }

    private OrderCreatedEvent buildEvent(String orderId) {
        return new OrderCreatedEvent(
            orderId, orderId + "-cust", "PENDING", BigDecimal.TEN,
            Collections.emptyList(), Instant.now(), null, null);
    }
}
