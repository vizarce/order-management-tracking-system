package com.ordertracking.orderservice.domain.model;

import com.ordertracking.orderservice.domain.model.valueobject.Email;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    // ── Creation ────────────────────────────────────────────────────────────

    @Test
    void shouldCreateCustomerSuccessfully() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        assertThat(customer.getName()).isEqualTo("Alice");
        assertThat(customer.getEmail().value()).isEqualTo("alice@example.com");
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldAssignUniqueIdsToDistinctCustomers() {
        Customer c1 = Customer.create("Alice", "alice@example.com");
        Customer c2 = Customer.create("Bob", "bob@example.com");
        assertThat(c1.getId()).isNotEqualTo(c2.getId());
    }

    // ── Email validation ────────────────────────────────────────────────────

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> Customer.create("Bob", "not-an-email"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email");
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() -> Customer.create("Bob", ""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> Customer.create("Bob", null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmailWithoutDomain() {
        assertThatThrownBy(() -> Customer.create("Bob", "user@"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Duplicate email (value-object equality) ──────────────────────────────
    // Domain rule: two Email instances wrapping the same address are considered
    // equal (record equality). Enforcement of uniqueness across persisted
    // customers is the responsibility of the repository layer.

    @Test
    void shouldTreatSameEmailAddressAsEqual() {
        Email e1 = Email.of("alice@example.com");
        Email e2 = Email.of("alice@example.com");
        assertThat(e1).isEqualTo(e2);
    }

    @Test
    void shouldTreatDifferentEmailAddressesAsNotEqual() {
        Email e1 = Email.of("alice@example.com");
        Email e2 = Email.of("bob@example.com");
        assertThat(e1).isNotEqualTo(e2);
    }

    // ── Update ──────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateCustomerName() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        customer.update("Alice Updated", null);
        assertThat(customer.getName()).isEqualTo("Alice Updated");
        assertThat(customer.getEmail().value()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldUpdateCustomerEmail() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        customer.update(null, "newalice@example.com");
        assertThat(customer.getName()).isEqualTo("Alice");
        assertThat(customer.getEmail().value()).isEqualTo("newalice@example.com");
    }

    @Test
    void shouldNotOverwriteNameWithBlankOnUpdate() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        customer.update("   ", null);
        assertThat(customer.getName()).isEqualTo("Alice");
    }

    @Test
    void shouldNotOverwriteEmailWithBlankOnUpdate() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        customer.update(null, "   ");
        assertThat(customer.getEmail().value()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldRejectInvalidEmailOnUpdate() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        assertThatThrownBy(() -> customer.update(null, "bad-email"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
