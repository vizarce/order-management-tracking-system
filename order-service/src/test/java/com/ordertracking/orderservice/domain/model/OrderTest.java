package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.exception.DomainException;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void shouldCreateOrderWithPendingStatus() {
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        Order order = Order.create(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    void shouldAddItemAndCalculateTotal() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        Money price = Money.of(BigDecimal.valueOf(10.00), "USD");
        order.addItem(new OrderItem("p1", "Product 1", 3, price));
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldConfirmOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldCancelOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldNotAllowAddingItemsToNonPendingOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        Money price = Money.of(BigDecimal.valueOf(5.00), "USD");
        assertThatThrownBy(() -> order.addItem(new OrderItem("p2", "Product 2", 1, price)))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCancelDeliveredOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        order.ship();
        order.deliver();
        assertThatThrownBy(order::cancel).isInstanceOf(DomainException.class);
    }
}
