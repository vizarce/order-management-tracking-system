package com.ordertracking.orderservice.application.usecase.impl;

import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.usecase.CreateOrderUseCase;
import com.ordertracking.orderservice.domain.exception.CustomerNotFoundException;
import com.ordertracking.orderservice.domain.exception.ProductNotFoundException;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.OrderItem;
import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.repository.CustomerRepository;
import com.ordertracking.orderservice.domain.repository.OrderRepository;
import com.ordertracking.orderservice.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CreateOrderUseCaseImpl(OrderRepository orderRepository,
                                  CustomerRepository customerRepository,
                                  ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Order execute(CreateOrderRequest request) {
        CustomerId customerId = CustomerId.of(request.customerId());
        customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));

        Order order = Order.create(customerId);

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException(itemRequest.productId()));
            product.reduceStock(itemRequest.quantity());
            productRepository.save(product);
            order.addItem(new OrderItem(product.getId(), product.getName(), itemRequest.quantity(), product.getPrice()));
        }

        return orderRepository.save(order);
    }
}
