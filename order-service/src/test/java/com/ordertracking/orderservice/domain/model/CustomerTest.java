package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.model.valueobject.Email;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomerSuccessfully() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        assertThat(customer.getName()).isEqualTo("Alice");
        assertThat(customer.getEmail().value()).isEqualTo("alice@example.com");
        assertThat(customer.getId()).isNotNull();
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> Customer.create("Bob", "not-an-email"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUpdateCustomerName() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        customer.update("Alice Updated", null);
        assertThat(customer.getName()).isEqualTo("Alice Updated");
        assertThat(customer.getEmail().value()).isEqualTo("alice@example.com");
    }
}
