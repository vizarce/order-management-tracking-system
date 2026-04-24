package com.ordertracking.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Shared response DTO returned by the tracking service's REST API.
 * Used by the Feign client in order-service to receive type-safe responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackingResponse(
    String orderId,
    String customerId,
    String status,
    BigDecimal totalAmount,
    List<OrderItemDto> items,
    Instant createdAt,
    Instant updatedAt
) {}
