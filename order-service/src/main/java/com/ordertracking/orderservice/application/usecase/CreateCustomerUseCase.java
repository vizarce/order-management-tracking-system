package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.domain.model.Customer;

public interface CreateCustomerUseCase {
    Customer execute(CreateCustomerRequest request);
}
