package com.ordertracking.orderservice.domain.model.valueobject;

import java.util.UUID;

public record OrderId(UUID value) {
    public OrderId {
        if (value == null) throw new IllegalArgumentException("OrderId value must not be null");
    }
    public static OrderId of(UUID value) { return new OrderId(value); }
    public static OrderId of(String value) { return new OrderId(UUID.fromString(value)); }
    public static OrderId generate() { return new OrderId(UUID.randomUUID()); }
    @Override public String toString() { return value.toString(); }
}
