package com.ordertracking.orderservice.domain.model.valueobject;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Email must not be blank");
        if (!EMAIL_PATTERN.matcher(value).matches()) throw new IllegalArgumentException("Invalid email: " + value);
    }
    public static Email of(String value) { return new Email(value); }
    @Override public String toString() { return value; }
}
