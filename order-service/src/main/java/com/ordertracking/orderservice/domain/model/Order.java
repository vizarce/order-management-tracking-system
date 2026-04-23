package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.exception.DomainException;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items = new ArrayList<>();
    private OrderStatus status;
    private Money totalAmount;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(OrderId id, CustomerId customerId) {
        this.id = id;
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.totalAmount = Money.zero("USD");
        this.createdAt = LocalDateTime.now();
    }

    public static Order create(CustomerId customerId) {
        return new Order(OrderId.generate(), customerId);
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) throw new DomainException("Cannot add items to order in status: " + status);
        items.add(item);
        recalculateTotal();
    }

    public void calculateTotal() { recalculateTotal(); }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.zero("USD"), Money::add);
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) throw new DomainException("Order can only be confirmed from PENDING state");
        this.status = OrderStatus.PROCESSING;
    }

    public void ship() {
        if (status != OrderStatus.PROCESSING) throw new DomainException("Order can only be shipped from PROCESSING state");
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (status != OrderStatus.SHIPPED) throw new DomainException("Order can only be delivered from SHIPPED state");
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED) throw new DomainException("Cannot cancel a delivered order");
        this.status = OrderStatus.CANCELLED;
    }

    public OrderId getId() { return id; }
    public void setId(OrderId id) { this.id = id; }
    public CustomerId getCustomerId() { return customerId; }
    public void setCustomerId(CustomerId customerId) { this.customerId = customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public void setItems(List<OrderItem> items) { this.items = new ArrayList<>(items); recalculateTotal(); }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Money getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Money totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
