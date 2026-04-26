package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.application.dto.CreateCustomerRequest;
import com.ordertracking.orderservice.application.dto.CreateCustomerResponse;
import com.ordertracking.orderservice.application.usecase.CreateCustomerUseCase;
import com.ordertracking.orderservice.domain.exception.CustomerNotFoundException;
import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerApplicationServiceTest {

    @Mock private CreateCustomerUseCase createCustomerUseCase;
    @Mock private CustomerRepository customerRepository;

    private CustomerApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CustomerApplicationService(createCustomerUseCase, customerRepository);
    }

    @Test
    void createCustomer_delegatesToUseCaseAndReturnsResponse() {
        var request = new CreateCustomerRequest("Alice", "alice@example.com");
        Customer customer = Customer.create("Alice", "alice@example.com");
        when(createCustomerUseCase.execute(request)).thenReturn(customer);

        CreateCustomerResponse response = service.createCustomer(request);

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        verify(createCustomerUseCase).execute(request);
    }

    @Test
    void getCustomer_returnsResponseWhenFound() {
        CustomerId id = CustomerId.of(UUID.randomUUID());
        Customer customer = Customer.create("Bob", "bob@example.com");
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));

        CreateCustomerResponse response = service.getCustomer(id.toString());

        assertThat(response.name()).isEqualTo("Bob");
    }

    @Test
    void getCustomer_throwsWhenNotFound() {
        String id = UUID.randomUUID().toString();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCustomer(id))
            .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void getAllCustomers_returnsMappedList() {
        Customer customer = Customer.create("Carol", "carol@example.com");
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CreateCustomerResponse> result = service.getAllCustomers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Carol");
    }
}
