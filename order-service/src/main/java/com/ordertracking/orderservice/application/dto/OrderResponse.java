package com.ordertracking.orderservice.application.dto;

import com.ordertracking.orderservice.domain.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
    String orderId,
    String customerId,
    String status,
    BigDecimal totalAmount,
    List<OrderItemDetail> items,
    LocalDateTime createdAt
) {
    public record OrderItemDetail(String productId, String productName, int quantity, BigDecimal unitPrice) {}

    public static OrderResponse from(Order order) {
        List<OrderItemDetail> itemDetails = order.getItems().stream()
            .map(i -> new OrderItemDetail(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice().amount()))
            .collect(Collectors.toList());
        return new OrderResponse(
            order.getId().toString(),
            order.getCustomerId().toString(),
            order.getStatus().name(),
            order.getTotalAmount().amount(),
            itemDetails,
            order.getCreatedAt()
        );
    }
}
