package com.ordertracking.trackingservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.common.event.OrderCreatedEvent;
import com.ordertracking.common.mdc.MdcConstants;
import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final OrderTrackingService orderTrackingService;

    public OrderEventConsumer(ObjectMapper objectMapper, OrderTrackingService orderTrackingService) {
        this.objectMapper = objectMapper;
        this.orderTrackingService = orderTrackingService;
    }

    /**
     * Topic is read from the application property so it stays consistent with the producer
     * and can be changed per-environment without recompiling.
     */
    @KafkaListener(topics = "${kafka.topics.orders-events:orders.events}",
                   groupId = "${spring.kafka.consumer.group-id:tracking-service-group}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            // Restore MDC from Kafka headers — blank/empty values are treated as absent
            // to prevent blank traceIds from appearing in logs.
            String traceId = extractHeader(record, MdcConstants.HEADER_TRACE_ID);
            MDC.put(MdcConstants.TRACE_ID,   (traceId   != null && !traceId.isBlank())   ? traceId   : UUID.randomUUID().toString());
            String requestId = extractHeader(record, MdcConstants.HEADER_REQUEST_ID);
            if (requestId != null && !requestId.isBlank()) MDC.put(MdcConstants.REQUEST_ID, requestId);

            MDC.put(MdcConstants.TOPIC,     record.topic());
            MDC.put(MdcConstants.PARTITION, String.valueOf(record.partition()));
            MDC.put(MdcConstants.OFFSET,    String.valueOf(record.offset()));

            log.info("Processing Kafka message");
            processOrder(record.value());
            log.info("Message processed");
        } finally {
            MDC.clear();
        }
    }

    private void processOrder(String value) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(value, OrderCreatedEvent.class);

            List<OrderTrackingDto.TrackingItemDto> items = event.items() != null
                ? event.items().stream()
                    .map(i -> new OrderTrackingDto.TrackingItemDto(i.productId(), i.productName(), i.quantity(), i.unitPrice()))
                    .collect(Collectors.toList())
                : Collections.emptyList();

            OrderTrackingDto dto = new OrderTrackingDto(
                event.orderId(),
                event.customerId(),
                event.status(),
                event.totalAmount(),
                items,
                event.occurredAt() != null ? event.occurredAt() : Instant.now(),
                Instant.now()
            );

            // Block with timeout so Kafka offset is committed only after the tracking
            // record is actually persisted.  Using subscribe() would make this
            // fire-and-forget, risking message loss on restart if the write fails.
            orderTrackingService.saveTracking(dto)
                .doOnSuccess(saved -> log.info("Tracking saved for orderId={}", saved.orderId()))
                .doOnError(e -> log.error("Failed to save tracking for orderId={}", event.orderId(), e))
                .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("Failed to process order event: {}", value, e);
        }
    }

    private String extractHeader(ConsumerRecord<String, String> record, String headerName) {
        if (record.headers() == null) return null;
        var header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) return null;
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
