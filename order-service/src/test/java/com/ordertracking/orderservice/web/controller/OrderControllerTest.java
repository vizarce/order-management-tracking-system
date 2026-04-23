package com.ordertracking.orderservice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.orderservice.application.dto.CreateOrderRequest;
import com.ordertracking.orderservice.application.dto.CreateOrderResponse;
import com.ordertracking.orderservice.application.service.OrderApplicationService;
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
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value("order-1"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnBadRequestForMissingCustomerId() throws Exception {
        var request = new CreateOrderRequest(null, List.of());
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
