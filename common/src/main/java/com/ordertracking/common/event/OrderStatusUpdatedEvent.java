package com.ordertracking.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderStatusUpdatedEvent(
    String orderId,
    String previousStatus,
    String newStatus,
    Instant occurredAt,
    String traceId,
    String requestId
) {}
