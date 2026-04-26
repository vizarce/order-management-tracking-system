package com.ordertracking.orderservice.application.service;

import com.ordertracking.orderservice.application.dto.CreateProductRequest;
import com.ordertracking.orderservice.application.dto.CreateProductResponse;
import com.ordertracking.orderservice.application.usecase.CreateProductUseCase;
import com.ordertracking.orderservice.domain.exception.ProductNotFoundException;
import com.ordertracking.orderservice.domain.model.Product;
import com.ordertracking.orderservice.domain.model.valueobject.Money;
import com.ordertracking.orderservice.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

    @Mock private CreateProductUseCase createProductUseCase;
    @Mock private ProductRepository productRepository;

    private ProductApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ProductApplicationService(createProductUseCase, productRepository);
    }

    @Test
    void createProduct_delegatesToUseCaseAndReturnsResponse() {
        var request = new CreateProductRequest("Widget", "A widget", BigDecimal.TEN, "USD", 100);
        Product product = Product.create("Widget", "A widget", Money.of(BigDecimal.TEN, "USD"), 100);
        when(createProductUseCase.execute(request)).thenReturn(product);

        CreateProductResponse response = service.createProduct(request);

        assertThat(response.name()).isEqualTo("Widget");
        assertThat(response.price()).isEqualByComparingTo("10");
        assertThat(response.stockQuantity()).isEqualTo(100);
        verify(createProductUseCase).execute(request);
    }

    @Test
    void getProduct_returnsResponseWhenFound() {
        String id = UUID.randomUUID().toString();
        Product product = Product.create("Gadget", "A gadget", Money.of(BigDecimal.valueOf(20), "USD"), 50);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        CreateProductResponse response = service.getProduct(id);

        assertThat(response.name()).isEqualTo("Gadget");
    }

    @Test
    void getProduct_throwsWhenNotFound() {
        String id = UUID.randomUUID().toString();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProduct(id))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getAllProducts_returnsMappedList() {
        Product product = Product.create("Gizmo", "A gizmo", Money.of(BigDecimal.valueOf(5), "USD"), 200);
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<CreateProductResponse> result = service.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Gizmo");
    }
}
