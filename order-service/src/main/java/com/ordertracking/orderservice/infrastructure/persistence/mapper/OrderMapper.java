package com.ordertracking.orderservice.infrastructure.persistence.mapper;

import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.OrderItem;
import com.ordertracking.orderservice.domain.model.OrderStatus;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import com.ordertracking.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.ordertracking.orderservice.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().value());
        entity.setCustomerId(order.getCustomerId().value());
        entity.setStatus(order.getStatus().name());
        entity.setTotalAmount(order.getTotalAmount().amount());
        entity.setTotalCurrency(order.getTotalAmount().currency());
        entity.setCreatedAt(order.getCreatedAt());

        List<OrderItemEntity> itemEntities = order.getItems().stream().map(item -> {
            OrderItemEntity ie = new OrderItemEntity();
            ie.setOrder(entity);
            ie.setProductId(item.getProductId());
            ie.setProductName(item.getProductName());
            ie.setQuantity(item.getQuantity());
            ie.setUnitPrice(item.getUnitPrice().amount());
            ie.setUnitPriceCurrency(item.getUnitPrice().currency());
            return ie;
        }).collect(Collectors.toList());
        entity.setItems(itemEntities);
        return entity;
    }

    /**
     * Updates mutable fields on an already-persistent (managed) entity.
     *
     * <p>By updating in place we avoid the delete-then-reinsert cycle that
     * would occur if we always replaced the entity reference.  Order items are
     * intentionally not modified here because they are immutable after order
     * creation; only the status and monetary total can change over the order
     * life-cycle.  The {@code createdAt} timestamp is also left untouched
     * because it must remain stable for the lifetime of the order.</p>
     */
    public void updateEntity(Order order, OrderEntity entity) {
        entity.setStatus(order.getStatus().name());
        entity.setTotalAmount(order.getTotalAmount().amount());
        entity.setTotalCurrency(order.getTotalAmount().currency());
    }

    public Order toDomain(OrderEntity entity) {
        Order order = new Order();
        order.setId(OrderId.of(entity.getId()));
        order.setCustomerId(CustomerId.of(entity.getCustomerId()));
        order.setStatus(OrderStatus.valueOf(entity.getStatus()));
        String currency = entity.getTotalCurrency() != null ? entity.getTotalCurrency() : "USD";
        order.setTotalAmount(Money.of(entity.getTotalAmount() != null ? entity.getTotalAmount() : java.math.BigDecimal.ZERO, currency));
        order.setCreatedAt(entity.getCreatedAt());

        List<OrderItem> items = entity.getItems().stream().map(ie -> {
            String itemCurrency = ie.getUnitPriceCurrency() != null ? ie.getUnitPriceCurrency() : "USD";
            return new OrderItem(ie.getProductId(), ie.getProductName(), ie.getQuantity(), Money.of(ie.getUnitPrice(), itemCurrency));
        }).collect(Collectors.toList());
        order.setItems(items);
        return order;
    }
}
