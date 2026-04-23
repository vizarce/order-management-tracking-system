package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.model.valueobject.Money;

public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private Money unitPrice;

    public OrderItem() {}

    public OrderItem(String productId, String productName, int quantity, Money unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Money getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Money unitPrice) { this.unitPrice = unitPrice; }
}
