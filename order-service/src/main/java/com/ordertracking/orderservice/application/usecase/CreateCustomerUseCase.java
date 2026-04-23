package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.repository.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class CreateCustomerUseCase {
    private final CustomerRepository customerRepository;

    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer execute(CreateCustomerRequest request) {
        Customer customer = Customer.create(request.name(), request.email());
        return customerRepository.save(customer);
    }
}
