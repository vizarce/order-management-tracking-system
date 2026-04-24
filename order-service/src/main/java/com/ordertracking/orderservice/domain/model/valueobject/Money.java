package com.ordertracking.orderservice.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null) throw new IllegalArgumentException("Amount must not be null");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency must not be blank");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    public static Money of(BigDecimal amount, String currency) { return new Money(amount, currency); }
    public static Money of(double amount, String currency) { return new Money(BigDecimal.valueOf(amount), currency); }
    public static Money zero(String currency) { return new Money(BigDecimal.ZERO, currency); }
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    public Money multiply(int multiplier) { return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency); }
    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
    }
}
