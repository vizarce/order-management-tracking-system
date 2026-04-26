package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.domain.model.Order;

public interface CreateOrderUseCase {
    Order execute(CreateOrderRequest request);
}
