package com.ordertracking.orderservice.domain.repository;

import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(CustomerId id);
    List<Customer> findAll();
    void deleteById(CustomerId id);
    boolean existsById(CustomerId id);
}
