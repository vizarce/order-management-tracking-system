package com.ordertracking.trackingservice.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "order_trackings")
public class OrderTrackingDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;

    private String customerId;
    private String status;
    private BigDecimal totalAmount;
    private List<TrackingItemDoc> items;
    private List<TrackingEventDoc> eventLog;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderTrackingDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<TrackingItemDoc> getItems() { return items; }
    public void setItems(List<TrackingItemDoc> items) { this.items = items; }
    public List<TrackingEventDoc> getEventLog() { return eventLog; }
    public void setEventLog(List<TrackingEventDoc> eventLog) { this.eventLog = eventLog; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class TrackingItemDoc {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public TrackingItemDoc() {}
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }

    public static class TrackingEventDoc {
        private Instant timestamp;
        private String status;
        private String description;

        public TrackingEventDoc() {}
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
