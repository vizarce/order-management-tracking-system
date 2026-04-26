package com.ordertracking.orderservice.infrastructure.persistence;

import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.model.Order;
import com.ordertracking.orderservice.domain.model.OrderItem;
import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.model.valueobject.OrderId;
import com.ordertracking.orderservice.infrastructure.persistence.adapter.CustomerRepositoryAdapter;
import com.ordertracking.orderservice.infrastructure.persistence.adapter.OrderRepositoryAdapter;
import com.ordertracking.orderservice.infrastructure.persistence.adapter.ProductRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the JPA persistence adapters.
 *
 * <p>Uses {@code @DataJpaTest} to spin up only the JPA slice (H2 in-memory),
 * and imports the adapter + mapper classes so they are available as Spring beans.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({
    CustomerRepositoryAdapter.class,
    OrderRepositoryAdapter.class,
    ProductRepositoryAdapter.class,
    com.ordertracking.orderservice.infrastructure.persistence.mapper.CustomerMapper.class,
    com.ordertracking.orderservice.infrastructure.persistence.mapper.OrderMapper.class,
    com.ordertracking.orderservice.infrastructure.persistence.mapper.ProductMapper.class
})
class PersistenceAdapterIntegrationTest {

    @Autowired CustomerRepositoryAdapter customerAdapter;
    @Autowired OrderRepositoryAdapter orderAdapter;
    @Autowired ProductRepositoryAdapter productAdapter;

    // ── Customer ──────────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndFindCustomerById() {
        Customer customer = Customer.create("Alice", "alice@example.com");
        customerAdapter.save(customer);

        Optional<Customer> found = customerAdapter.findById(customer.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getEmail().value()).isEqualTo("alice@example.com");
    }

    @Test
    void shouldListAllCustomers() {
        customerAdapter.save(Customer.create("Bob", "bob@example.com"));
        customerAdapter.save(Customer.create("Carol", "carol@example.com"));

        List<Customer> all = customerAdapter.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteCustomerById() {
        Customer customer = Customer.create("Dave", "dave@example.com");
        customerAdapter.save(customer);
        assertThat(customerAdapter.existsById(customer.getId())).isTrue();

        customerAdapter.deleteById(customer.getId());

        assertThat(customerAdapter.existsById(customer.getId())).isFalse();
    }

    // ── Product ───────────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndFindProductById() {
        Product product = Product.create("Widget", "A small widget", Money.of(9.99, "USD"), 100);
        productAdapter.save(product);

        Optional<Product> found = productAdapter.findById(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Widget");
        assertThat(found.get().getPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(9.99));
        assertThat(found.get().getStockQuantity()).isEqualTo(100);
    }

    @Test
    void shouldUpdateProductStock() {
        Product product = Product.create("Gadget", "A gadget", Money.of(49.99, "USD"), 50);
        productAdapter.save(product);

        product.reduceStock(5);
        productAdapter.save(product);

        Product updated = productAdapter.findById(product.getId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(45);
    }

    @Test
    void shouldDeleteProductById() {
        Product product = Product.create("Throwaway", "desc", Money.of(1.00, "USD"), 1);
        productAdapter.save(product);

        productAdapter.deleteById(product.getId());

        assertThat(productAdapter.findById(product.getId())).isEmpty();
    }

    // ── Order ─────────────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndFindOrderById() {
        Customer customer = customerAdapter.save(Customer.create("Eve", "eve@example.com"));
        Product product = productAdapter.save(Product.create("Book", "A book", Money.of(12.00, "USD"), 10));

        Order order = Order.create(customer.getId());
        order.addItem(new OrderItem(product.getId(), product.getName(), 2, product.getPrice()));
        Order saved = orderAdapter.save(order);

        Optional<Order> found = orderAdapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(customer.getId());
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getProductName()).isEqualTo("Book");
        assertThat(found.get().getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldUpdateOrderStatusWithoutReplacingItems() {
        Customer customer = customerAdapter.save(Customer.create("Frank", "frank@example.com"));
        Product product = productAdapter.save(Product.create("Pen", "A pen", Money.of(1.50, "USD"), 20));

        Order order = Order.create(customer.getId());
        order.addItem(new OrderItem(product.getId(), product.getName(), 3, product.getPrice()));
        Order saved = orderAdapter.save(order);

        // Transition to PROCESSING (confirm) — items must not change
        saved.confirm();
        Order confirmed = orderAdapter.save(saved);

        assertThat(confirmed.getStatus().name()).isEqualTo("PROCESSING");
        assertThat(confirmed.getItems()).hasSize(1);
        assertThat(confirmed.getItems().get(0).getProductName()).isEqualTo("Pen");
    }

    @Test
    void shouldFindOrdersByCustomerId() {
        Customer customer = customerAdapter.save(Customer.create("Grace", "grace@example.com"));
        Product p = productAdapter.save(Product.create("Lamp", "A lamp", Money.of(25.00, "USD"), 5));

        Order o1 = Order.create(customer.getId());
        o1.addItem(new OrderItem(p.getId(), p.getName(), 1, p.getPrice()));
        orderAdapter.save(o1);

        Order o2 = Order.create(customer.getId());
        o2.addItem(new OrderItem(p.getId(), p.getName(), 2, p.getPrice()));
        orderAdapter.save(o2);

        List<Order> orders = orderAdapter.findByCustomerId(customer.getId());

        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getCustomerId().equals(customer.getId()));
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        Optional<Order> result = orderAdapter.findById(OrderId.of(UUID.randomUUID()));
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCustomerNotFound() {
        Optional<Customer> result = customerAdapter.findById(CustomerId.of(UUID.randomUUID()));
        assertThat(result).isEmpty();
    }
}
