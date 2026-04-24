package com.ordertracking.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDto(
    String id,
    String name,
    String description,
    BigDecimal price,
    String currency,
    Integer stockQuantity
) {}
