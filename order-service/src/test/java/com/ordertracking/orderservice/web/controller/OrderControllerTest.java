package com.ordertracking.orderservice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.dto.CreateOrderResponse;
import com.ordertracking.orderservice.application.dto.OrderResponse;
import com.ordertracking.orderservice.application.service.OrderApplicationService;
import com.ordertracking.orderservice.domain.exception.InsufficientStockException;
import com.ordertracking.orderservice.domain.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderApplicationService orderApplicationService;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        var request = new CreateOrderRequest("cust-1", List.of(new CreateOrderRequest.OrderItemRequest("prod-1", 2)));
        var response = new CreateOrderResponse("order-1", "cust-1", "PENDING", BigDecimal.valueOf(20.00), List.of(), LocalDateTime.now());
        when(orderApplicationService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.orderId").value("order-1"))
            .andExpect(jsonPath("$.customerId").value("cust-1"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(20.00));
    }

    @Test
    void shouldReturnBadRequestForMissingCustomerId() throws Exception {
        var request = new CreateOrderRequest(null, List.of(new CreateOrderRequest.OrderItemRequest("prod-1", 1)));
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForEmptyItemsList() throws Exception {
        var request = new CreateOrderRequest("cust-1", List.of());
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        var response = new OrderResponse("order-1", "cust-1", "CONFIRMED", BigDecimal.valueOf(20.00), List.of(), LocalDateTime.now());
        when(orderApplicationService.getOrder(eq("order-1"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/order-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value("order-1"))
            .andExpect(jsonPath("$.customerId").value("cust-1"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.totalAmount").value(20.00));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(orderApplicationService.getOrder(eq("unknown")))
            .thenThrow(new OrderNotFoundException("unknown"));

        mockMvc.perform(get("/api/v1/orders/unknown"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenInsufficientStock() throws Exception {
        var request = new CreateOrderRequest("cust-1", List.of(new CreateOrderRequest.OrderItemRequest("prod-1", 10)));
        when(orderApplicationService.createOrder(any()))
            .thenThrow(new InsufficientStockException("prod-1", 10, 3));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }
}
