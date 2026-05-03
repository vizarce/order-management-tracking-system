package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.exception.DomainException;
import com.ordertracking.orderservice.domain.exception.InsufficientStockException;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private static final Money PRICE_10 = Money.of(BigDecimal.valueOf(10.00), "USD");
    private static final Money PRICE_5  = Money.of(BigDecimal.valueOf(5.00),  "USD");

    // ── Creation ────────────────────────────────────────────────────────────

    @Test
    void shouldCreateOrderWithPendingStatus() {
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        Order order = Order.create(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).isEmpty();
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("0.00");
        assertThat(order.getCreatedAt()).isNotNull();
    }

    // ── Items & total ────────────────────────────────────────────────────────

    @Test
    void shouldAddItemAndCalculateTotal() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.addItem(new OrderItem("p1", "Product 1", 3, PRICE_10));
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldAccumulateTotalAcrossMultipleItems() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.addItem(new OrderItem("p1", "Product 1", 2, PRICE_10)); // 20
        order.addItem(new OrderItem("p2", "Product 2", 4, PRICE_5));  // 20
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("40.00");
    }

    @Test
    void shouldNotAllowAddingItemsToNonPendingOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        assertThatThrownBy(() -> order.addItem(new OrderItem("p2", "Product 2", 1, PRICE_5)))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("PROCESSING");
    }

    // ── Status transitions ───────────────────────────────────────────────────

    @Test
    void shouldConfirmOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldShipOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        order.ship();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void shouldDeliverOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        order.ship();
        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldCancelProcessingOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    // ── Invalid transitions ──────────────────────────────────────────────────

    @Test
    void shouldNotShipFromPendingState() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        assertThatThrownBy(order::ship)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("PROCESSING");
    }

    @Test
    void shouldNotDeliverFromProcessingState() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        assertThatThrownBy(order::deliver)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("SHIPPED");
    }

    @Test
    void shouldNotConfirmAlreadyProcessingOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        assertThatThrownBy(order::confirm)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("PENDING");
    }

    @Test
    void shouldNotCancelDeliveredOrder() {
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));
        order.confirm();
        order.ship();
        order.deliver();
        assertThatThrownBy(order::cancel).isInstanceOf(DomainException.class);
    }

    // ── Insufficient stock scenario ──────────────────────────────────────────
    // Stock is checked at the use-case level via Product.reduceStock(); the
    // domain rule is that an InsufficientStockException prevents the order
    // item from being created, leaving the order unmodified.

    @Test
    void shouldLeaveOrderEmptyWhenStockIsInsufficient() {
        Product product = Product.create("Widget", "desc", PRICE_10, 1);
        Order order = Order.create(CustomerId.of(UUID.randomUUID()));

        // Attempting to reduce more stock than available must throw.
        assertThatThrownBy(() -> product.reduceStock(5))
            .isInstanceOf(InsufficientStockException.class);

        // Order must remain untouched.
        assertThat(order.getItems()).isEmpty();
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("0.00");
    }
}
