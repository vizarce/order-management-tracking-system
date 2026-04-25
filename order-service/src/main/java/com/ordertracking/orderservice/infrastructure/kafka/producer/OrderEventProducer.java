package com.ordertracking.orderservice.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.common.dto.OrderItemDto;
import com.ordertracking.common.event.OrderCreatedEvent;
import com.ordertracking.common.event.OrderStatusUpdatedEvent;
import com.ordertracking.common.mdc.MdcConstants;
import com.ordertracking.orderservice.domain.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderEventProducer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.orders-events:orders.events}")
    private String ordersTopic;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Order order) {
        try {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice().amount()))
                .collect(Collectors.toList());

            String traceId   = MDC.get(MdcConstants.TRACE_ID);
            String requestId = MDC.get(MdcConstants.REQUEST_ID);

            OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId().toString(),
                order.getCustomerId().toString(),
                order.getStatus().name(),
                order.getTotalAmount().amount(),
                itemDtos,
                Instant.now(),
                traceId,
                requestId
            );

            String payload = objectMapper.writeValueAsString(event);

            var builder = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, ordersTopic)
                .setHeader(KafkaHeaders.KEY, order.getId().toString());

            // Only propagate MDC headers when a value is actually present — an empty
            // byte array would be decoded as a blank string on the consumer side and
            // silently replace the generated trace ID with an empty one.
            if (traceId   != null) builder.setHeader(MdcConstants.HEADER_TRACE_ID,   traceId.getBytes(StandardCharsets.UTF_8));
            if (requestId != null) builder.setHeader(MdcConstants.HEADER_REQUEST_ID, requestId.getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(builder.build());
            log.info("Published OrderCreatedEvent for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for order {}", order.getId(), e);
        }
    }

    public void publishOrderStatusUpdated(Order order, String previousStatus) {
        try {
            String traceId   = MDC.get(MdcConstants.TRACE_ID);
            String requestId = MDC.get(MdcConstants.REQUEST_ID);

            OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
                order.getId().toString(),
                previousStatus,
                order.getStatus().name(),
                Instant.now(),
                traceId,
                requestId
            );

            String payload = objectMapper.writeValueAsString(event);

            var builder = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, ordersTopic)
                .setHeader(KafkaHeaders.KEY, order.getId().toString());

            if (traceId   != null) builder.setHeader(MdcConstants.HEADER_TRACE_ID,   traceId.getBytes(StandardCharsets.UTF_8));
            if (requestId != null) builder.setHeader(MdcConstants.HEADER_REQUEST_ID, requestId.getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(builder.build());
            log.info("Published OrderStatusUpdatedEvent for order {} ({} -> {})", order.getId(), previousStatus, order.getStatus().name());
        } catch (Exception e) {
            log.error("Failed to publish OrderStatusUpdatedEvent for order {}", order.getId(), e);
        }
    }
}
