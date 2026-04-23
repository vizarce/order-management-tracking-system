package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.exception.InsufficientStockException;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import java.util.UUID;

public class Product {
    private String id;
    private String name;
    private String description;
    private Money price;
    private Integer stockQuantity;

    public Product() {}

    public Product(String id, String name, String description, Money price, Integer stockQuantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public static Product create(String name, String description, Money price, int stock) {
        return new Product(UUID.randomUUID().toString(), name, description, price, stock);
    }

    public void reduceStock(int quantity) {
        if (this.stockQuantity < quantity) throw new InsufficientStockException(id, quantity, stockQuantity);
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public boolean isAvailable(int quantity) {
        return this.stockQuantity >= quantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Money getPrice() { return price; }
    public void setPrice(Money price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
