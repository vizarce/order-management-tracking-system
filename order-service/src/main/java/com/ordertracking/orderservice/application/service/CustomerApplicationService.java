package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.application.dto.CreateCustomerResponse;
import com.ordertracking.orderservice.application.usecase.CreateCustomerUseCase;
import com.ordertracking.orderservice.domain.exception.CustomerNotFoundException;
import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerApplicationService {
    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerRepository customerRepository;

    public CustomerApplicationService(CreateCustomerUseCase createCustomerUseCase, CustomerRepository customerRepository) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CreateCustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = createCustomerUseCase.execute(request);
        return CreateCustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public CreateCustomerResponse getCustomer(String id) {
        Customer customer = customerRepository.findById(CustomerId.of(id))
            .orElseThrow(() -> new CustomerNotFoundException(id));
        return CreateCustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public List<CreateCustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(CreateCustomerResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCustomer(String id) {
        customerRepository.deleteById(CustomerId.of(id));
    }
}
