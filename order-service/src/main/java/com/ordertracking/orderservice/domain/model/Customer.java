package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Email;
import java.time.LocalDateTime;

public class Customer {
    private CustomerId id;
    private String name;
    private Email email;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(CustomerId id, String name, Email email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    public static Customer create(String name, String email) {
        return new Customer(CustomerId.generate(), name, Email.of(email));
    }

    public void update(String name, String email) {
        if (name != null && !name.isBlank()) this.name = name;
        if (email != null && !email.isBlank()) this.email = Email.of(email);
    }

    public CustomerId getId() { return id; }
    public void setId(CustomerId id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
