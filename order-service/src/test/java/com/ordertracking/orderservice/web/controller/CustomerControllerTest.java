package com.ordertracking.orderservice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.application.dto.CreateCustomerResponse;
import com.ordertracking.orderservice.application.service.CustomerApplicationService;
import com.ordertracking.orderservice.domain.exception.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CustomerApplicationService customerApplicationService;

    @Test
    void shouldCreateCustomer() throws Exception {
        var request = new CreateCustomerRequest("Alice", "alice@example.com");
        var response = new CreateCustomerResponse("id-1", "Alice", "alice@example.com");
        when(customerApplicationService.createCustomer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        var response = new CreateCustomerResponse("id-1", "Alice", "alice@example.com");
        when(customerApplicationService.getCustomer(eq("id-1"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/id-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("id-1"))
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        when(customerApplicationService.getCustomer(eq("unknown")))
            .thenThrow(new CustomerNotFoundException("unknown"));

        mockMvc.perform(get("/api/v1/customers/unknown"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreateCustomerValidationFails() throws Exception {
        var request = new CreateCustomerRequest("", "not-an-email");

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllCustomers() throws Exception {
        when(customerApplicationService.getAllCustomers()).thenReturn(List.of(
            new CreateCustomerResponse("id-1", "Alice", "alice@example.com")
        ));
        mockMvc.perform(get("/api/v1/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Alice"));
    }
}
