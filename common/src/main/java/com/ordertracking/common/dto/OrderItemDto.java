package com.ordertracking.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItemDto(
    String productId,
    String productName,
    int quantity,
    BigDecimal unitPrice
) {}
