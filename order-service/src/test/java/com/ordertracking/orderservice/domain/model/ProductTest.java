package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.exception.InsufficientStockException;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProduct() {
        Product product = Product.create("Laptop", "A laptop", Money.of(BigDecimal.valueOf(999.99), "USD"), 10);
        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(product.isAvailable(5)).isTrue();
    }

    @Test
    void shouldReduceStock() {
        Product product = Product.create("Laptop", "A laptop", Money.of(BigDecimal.valueOf(999.99), "USD"), 10);
        product.reduceStock(3);
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void shouldThrowOnInsufficientStock() {
        Product product = Product.create("Laptop", "A laptop", Money.of(BigDecimal.valueOf(999.99), "USD"), 2);
        assertThatThrownBy(() -> product.reduceStock(5))
            .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void shouldIncreaseStock() {
        Product product = Product.create("Laptop", "desc", Money.of(BigDecimal.valueOf(100), "USD"), 5);
        product.increaseStock(3);
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }
}
