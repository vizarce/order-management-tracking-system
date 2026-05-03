package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.exception.DomainException;
import com.ordertracking.orderservice.domain.exception.InsufficientStockException;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private static final Money PRICE = Money.of(BigDecimal.valueOf(999.99), "USD");

    // ── Creation ────────────────────────────────────────────────────────────

    @Test
    void shouldCreateProduct() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 10);
        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getDescription()).isEqualTo("A laptop");
        assertThat(product.getPrice()).isEqualTo(PRICE);
        assertThat(product.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void shouldReportAvailableWhenStockSufficient() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 10);
        assertThat(product.isAvailable(5)).isTrue();
        assertThat(product.isAvailable(10)).isTrue();
    }

    @Test
    void shouldReportUnavailableWhenStockInsufficient() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 3);
        assertThat(product.isAvailable(5)).isFalse();
    }

    // ── Stock decrease ───────────────────────────────────────────────────────

    @Test
    void shouldReduceStock() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 10);
        product.reduceStock(3);
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void shouldReduceStockToZero() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 5);
        product.reduceStock(5);
        assertThat(product.getStockQuantity()).isZero();
    }

    @Test
    void shouldThrowOnInsufficientStock() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 2);
        assertThatThrownBy(() -> product.reduceStock(5))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock");
    }

    // ── Zero-stock guard ─────────────────────────────────────────────────────

    @Test
    void shouldRejectReduceStockWithZeroQuantity() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 10);
        assertThatThrownBy(() -> product.reduceStock(0))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("positive");
    }

    @Test
    void shouldRejectReduceStockWithNegativeQuantity() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 10);
        assertThatThrownBy(() -> product.reduceStock(-1))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("positive");
    }

    // ── Stock increase ───────────────────────────────────────────────────────

    @Test
    void shouldIncreaseStock() {
        Product product = Product.create("Laptop", "desc", Money.of(BigDecimal.valueOf(100), "USD"), 5);
        product.increaseStock(3);
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldRejectIncreaseStockWithZeroQuantity() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 5);
        assertThatThrownBy(() -> product.increaseStock(0))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("positive");
    }

    @Test
    void shouldRejectIncreaseStockWithNegativeQuantity() {
        Product product = Product.create("Laptop", "A laptop", PRICE, 5);
        assertThatThrownBy(() -> product.increaseStock(-3))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("positive");
    }

    @Test
    void shouldInitialiseNullStockToZeroOnIncrease() {
        Product product = new Product();
        product.increaseStock(5);
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }
}
