package com.ordertracking.trackingservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderTrackingDto(
    String orderId,
    String customerId,
    String status,
    BigDecimal totalAmount,
    List<TrackingItemDto> items,
    List<TrackingEventDto> eventLog,
    Instant createdAt,
    Instant updatedAt
) {
    public record TrackingItemDto(String productId, String productName, int quantity, BigDecimal unitPrice) {}
    public record TrackingEventDto(Instant timestamp, String status, String description) {}
}
