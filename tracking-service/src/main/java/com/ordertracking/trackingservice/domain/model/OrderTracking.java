package com.ordertracking.trackingservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderTracking {
    private String id;
    private String orderId;
    private String customerId;
    private TrackingStatus status;
    private BigDecimal totalAmount;
    private List<TrackingItem> items;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderTracking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public TrackingStatus getStatus() { return status; }
    public void setStatus(TrackingStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<TrackingItem> getItems() { return items; }
    public void setItems(List<TrackingItem> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class TrackingItem {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public TrackingItem() {}
        public TrackingItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
