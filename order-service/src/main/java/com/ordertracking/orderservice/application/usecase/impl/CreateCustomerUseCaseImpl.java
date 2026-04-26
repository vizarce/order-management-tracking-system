package com.ordertracking.orderservice.application.usecase.impl;

import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.application.usecase.CreateCustomerUseCase;
import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.repository.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {
    private final CustomerRepository customerRepository;

    public CreateCustomerUseCaseImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer execute(CreateCustomerRequest request) {
        Customer customer = Customer.create(request.name(), request.email());
        return customerRepository.save(customer);
    }
}
