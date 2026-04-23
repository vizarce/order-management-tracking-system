package com.ordertracking.orderservice.web.controller;

import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.dto.CreateOrderResponse;
import com.ordertracking.orderservice.application.dto.OrderResponse;
import com.ordertracking.orderservice.application.service.OrderApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderApplicationService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable String id) {
        return orderApplicationService.getOrder(id);
    }
}
