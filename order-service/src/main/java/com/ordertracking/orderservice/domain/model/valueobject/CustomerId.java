package com.ordertracking.orderservice.domain.model.valueobject;

import java.util.UUID;

public record CustomerId(UUID value) {
    public CustomerId {
        if (value == null) throw new IllegalArgumentException("CustomerId value must not be null");
    }
    public static CustomerId of(UUID value) { return new CustomerId(value); }
    public static CustomerId of(String value) { return new CustomerId(UUID.fromString(value)); }
    public static CustomerId generate() { return new CustomerId(UUID.randomUUID()); }
    @Override public String toString() { return value.toString(); }
}
