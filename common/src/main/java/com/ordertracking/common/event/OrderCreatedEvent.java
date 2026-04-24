package com.ordertracking.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ordertracking.common.dto.OrderItemDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
    String orderId,
    String customerId,
    String status,
    BigDecimal totalAmount,
    List<OrderItemDto> items,
    Instant occurredAt,
    String traceId,
    String requestId
) {}
