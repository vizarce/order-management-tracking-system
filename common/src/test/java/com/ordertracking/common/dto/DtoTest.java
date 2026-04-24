package com.ordertracking.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void customerDtoShouldHoldFields() {
        CustomerDto dto = new CustomerDto("c1", "Alice", "alice@example.com");
        assertThat(dto.id()).isEqualTo("c1");
        assertThat(dto.name()).isEqualTo("Alice");
        assertThat(dto.email()).isEqualTo("alice@example.com");
    }

    @Test
    void orderItemDtoShouldHoldFields() {
        OrderItemDto item = new OrderItemDto("p1", "Widget", 3, BigDecimal.valueOf(4.99));
        assertThat(item.productId()).isEqualTo("p1");
        assertThat(item.productName()).isEqualTo("Widget");
        assertThat(item.quantity()).isEqualTo(3);
        assertThat(item.unitPrice()).isEqualByComparingTo("4.99");
    }

    @Test
    void productDtoShouldHoldFields() {
        ProductDto dto = new ProductDto("p1", "Widget", "A small widget", BigDecimal.valueOf(9.99), "USD", 100);
        assertThat(dto.id()).isEqualTo("p1");
        assertThat(dto.name()).isEqualTo("Widget");
        assertThat(dto.description()).isEqualTo("A small widget");
        assertThat(dto.price()).isEqualByComparingTo("9.99");
        assertThat(dto.currency()).isEqualTo("USD");
        assertThat(dto.stockQuantity()).isEqualTo(100);
    }

    @Test
    void orderDtoShouldHoldFields() {
        Instant now = Instant.parse("2024-01-15T10:00:00Z");
        List<OrderItemDto> items = List.of(new OrderItemDto("p1", "Widget", 2, BigDecimal.valueOf(5.00)));
        OrderDto dto = new OrderDto("o1", "c1", items, "PENDING", BigDecimal.TEN, now);

        assertThat(dto.id()).isEqualTo("o1");
        assertThat(dto.customerId()).isEqualTo("c1");
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.status()).isEqualTo("PENDING");
        assertThat(dto.totalAmount()).isEqualByComparingTo("10");
        assertThat(dto.createdAt()).isEqualTo(now);
    }

    @Test
    void dtosShouldImplementValueEquality() {
        CustomerDto a = new CustomerDto("c1", "Alice", "alice@example.com");
        CustomerDto b = new CustomerDto("c1", "Alice", "alice@example.com");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        OrderItemDto ia = new OrderItemDto("p1", "Widget", 1, BigDecimal.ONE);
        OrderItemDto ib = new OrderItemDto("p1", "Widget", 1, BigDecimal.ONE);
        assertThat(ia).isEqualTo(ib);
    }
}
