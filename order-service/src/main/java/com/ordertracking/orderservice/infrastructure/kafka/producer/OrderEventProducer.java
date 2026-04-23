package com.ordertracking.orderservice.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.common.dto.OrderItemDto;
import com.ordertracking.common.event.OrderCreatedEvent;
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

            OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId().toString(),
                order.getCustomerId().toString(),
                order.getStatus().name(),
                order.getTotalAmount().amount(),
                itemDtos,
                Instant.now(),
                MDC.get("traceId"),
                MDC.get("requestId")
            );

            String payload = objectMapper.writeValueAsString(event);

            var message = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, ordersTopic)
                .setHeader(KafkaHeaders.KEY, order.getId().toString())
                .setHeader("X-Trace-Id", getHeaderBytes(MDC.get("traceId")))
                .setHeader("X-Request-Id", getHeaderBytes(MDC.get("requestId")))
                .build();

            kafkaTemplate.send(message);
            log.info("Published OrderCreatedEvent for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for order {}", order.getId(), e);
        }
    }

    private byte[] getHeaderBytes(String value) {
        return value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }
}
