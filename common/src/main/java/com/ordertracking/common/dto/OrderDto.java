package com.ordertracking.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDto(
    String id,
    String customerId,
    List<OrderItemDto> items,
    String status,
    BigDecimal totalAmount,
    Instant createdAt
) {}
