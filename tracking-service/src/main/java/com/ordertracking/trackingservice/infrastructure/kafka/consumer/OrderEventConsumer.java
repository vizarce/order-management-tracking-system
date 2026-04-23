package com.ordertracking.trackingservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.common.event.OrderCreatedEvent;
import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import com.ordertracking.trackingservice.domain.model.OrderTracking;
import com.ordertracking.trackingservice.infrastructure.persistence.mapper.OrderTrackingMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
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

    @KafkaListener(topics = "orders.events", groupId = "${spring.kafka.consumer.group-id:tracking-service-group}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            String traceId = extractHeader(record, "X-Trace-Id");
            MDC.put("traceId", traceId != null ? traceId : UUID.randomUUID().toString());
            MDC.put("topic", record.topic());
            MDC.put("partition", String.valueOf(record.partition()));
            MDC.put("offset", String.valueOf(record.offset()));

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

            orderTrackingService.saveTracking(dto)
                .doOnSuccess(saved -> log.info("Tracking saved for orderId={}", saved.orderId()))
                .doOnError(e -> log.error("Failed to save tracking for orderId={}", event.orderId(), e))
                .subscribe();
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
