package com.ordertracking.orderservice.infrastructure.persistence.mapper;

import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Email;
import com.ordertracking.orderservice.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerEntity toEntity(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(customer.getId().value());
        entity.setName(customer.getName());
        entity.setEmail(customer.getEmail().value());
        entity.setCreatedAt(customer.getCreatedAt());
        return entity;
    }

    public Customer toDomain(CustomerEntity entity) {
        Customer customer = new Customer();
        customer.setId(CustomerId.of(entity.getId()));
        customer.setName(entity.getName());
        customer.setEmail(Email.of(entity.getEmail()));
        customer.setCreatedAt(entity.getCreatedAt());
        return customer;
    }
}
