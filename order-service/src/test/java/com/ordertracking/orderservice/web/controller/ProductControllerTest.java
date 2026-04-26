package com.ordertracking.orderservice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.application.dto.CreateProductResponse;
import com.ordertracking.orderservice.application.service.ProductApplicationService;
import com.ordertracking.orderservice.domain.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductApplicationService productApplicationService;

    @Test
    void shouldCreateProduct() throws Exception {
        var request = new CreateProductRequest("Widget", "A nice widget", BigDecimal.valueOf(9.99), "USD", 100);
        var response = new CreateProductResponse("prod-1", "Widget", "A nice widget", BigDecimal.valueOf(9.99), "USD", 100);
        when(productApplicationService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("prod-1"))
            .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        var response = new CreateProductResponse("prod-1", "Widget", "A nice widget", BigDecimal.valueOf(9.99), "USD", 100);
        when(productApplicationService.getProduct(eq("prod-1"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/prod-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("prod-1"))
            .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productApplicationService.getProduct(eq("unknown")))
            .thenThrow(new ProductNotFoundException("unknown"));

        mockMvc.perform(get("/api/v1/products/unknown"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreateProductValidationFails() throws Exception {
        var request = new CreateProductRequest("", null, BigDecimal.valueOf(-1), null, 0);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
