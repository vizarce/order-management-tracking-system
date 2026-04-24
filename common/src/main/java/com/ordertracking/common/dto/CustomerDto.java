package com.ordertracking.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerDto(
    String id,
    String name,
    String email
) {}
