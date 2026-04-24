package com.ordertracking.orderservice.web.controller;

import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.application.dto.CreateCustomerResponse;
import com.ordertracking.orderservice.application.service.CustomerApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerApplicationService customerApplicationService;

    public CustomerController(CustomerApplicationService customerApplicationService) {
        this.customerApplicationService = customerApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerApplicationService.createCustomer(request);
    }

    @GetMapping("/{id}")
    public CreateCustomerResponse getCustomer(@PathVariable String id) {
        return customerApplicationService.getCustomer(id);
    }

    @GetMapping
    public List<CreateCustomerResponse> getAllCustomers() {
        return customerApplicationService.getAllCustomers();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable String id) {
        customerApplicationService.deleteCustomer(id);
    }
}
