package com.ordertracking.orderservice.application.usecase;

import com.ordertracking.orderservice.domain.model.Order;

public interface GetOrderUseCase {
    Order execute(String orderId);
}
