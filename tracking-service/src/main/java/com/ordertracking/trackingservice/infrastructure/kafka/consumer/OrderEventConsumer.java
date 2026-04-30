package com.ordertracking.trackingservice.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.common.event.OrderCreatedEvent;
import com.ordertracking.common.event.OrderStatusUpdatedEvent;
import com.ordertracking.common.mdc.MdcConstants;
import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
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
            String userId = extractHeader(record, MdcConstants.HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) MDC.put(MdcConstants.USER_ID, userId);

            MDC.put(MdcConstants.TOPIC,     record.topic());
            MDC.put(MdcConstants.PARTITION, String.valueOf(record.partition()));
            MDC.put(MdcConstants.OFFSET,    String.valueOf(record.offset()));

            log.info("Processing Kafka message");
            processEvent(record.value());
            log.info("Message processed");
        } finally {
            MDC.clear();
        }
    }

    /**
     * Dispatches the raw JSON payload to the appropriate handler based on the presence of
     * the {@code newStatus} field, which is exclusive to {@link OrderStatusUpdatedEvent}.
     * Both event types share the same topic without a dedicated event-type header, so a
     * structural field is used as a discriminator. If the schema evolves to include a typed
     * event-type field this method should be updated to use it instead.
     */
    private void processEvent(String value) {
        try {
            JsonNode node = objectMapper.readTree(value);
            if (node.has("newStatus")) {
                handleStatusUpdated(objectMapper.treeToValue(node, OrderStatusUpdatedEvent.class));
            } else {
                handleOrderCreated(objectMapper.treeToValue(node, OrderCreatedEvent.class));
            }
        } catch (Exception e) {
            log.error("Failed to process event: {}", value, e);
        }
    }

    /**
     * Handles {@link OrderCreatedEvent} idempotently: if a tracking document already exists
     * for the given orderId the event is silently skipped to ensure safe re-processing.
     */
    private void handleOrderCreated(OrderCreatedEvent event) {
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

        // Idempotent: skip creation when tracking already exists (e.g. duplicate delivery).
        // getTracking() throws NotFoundException when the order is not yet tracked; we
        // treat that as "not found" so the switchIfEmpty proceeds to saveTracking.
        // Block with timeout so Kafka offset is committed only after the write is confirmed.
        orderTrackingService.getTracking(event.orderId())
            .onErrorResume(com.ordertracking.trackingservice.domain.exception.NotFoundException.class,
                           e -> Mono.empty())
            .doOnNext(existing -> log.info("Tracking already exists for orderId={}, skipping (idempotent)", existing.orderId()))
            .switchIfEmpty(Mono.defer(() ->
                orderTrackingService.saveTracking(dto)
                    .doOnSuccess(saved -> log.info("Tracking saved for orderId={}", saved.orderId()))
                    .doOnError(e -> log.error("Failed to save tracking for orderId={}", event.orderId(), e))
            ))
            .block(Duration.ofSeconds(10));
    }

    /**
     * Handles {@link OrderStatusUpdatedEvent}: updates the status on the existing tracking
     * document and invalidates the Redis cache. The operation is naturally idempotent —
     * applying the same status update more than once yields the same final state.
     */
    private void handleStatusUpdated(OrderStatusUpdatedEvent event) {
        orderTrackingService.updateTrackingStatus(event.orderId(), event.newStatus())
            .doOnSuccess(updated -> log.info("Tracking status updated for orderId={} to {}", updated.orderId(), updated.status()))
            .switchIfEmpty(Mono.fromRunnable(() ->
                log.warn("No tracking found for orderId={}, status update skipped", event.orderId())
            ))
            .doOnError(e -> log.error("Failed to update tracking status for orderId={}", event.orderId(), e))
            .block(Duration.ofSeconds(10));
    }

    private String extractHeader(ConsumerRecord<String, String> record, String headerName) {
        if (record.headers() == null) return null;
        var header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) return null;
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
