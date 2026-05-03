package com.ordertracking.trackingservice.web.controller;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import com.ordertracking.trackingservice.domain.exception.NotFoundException;
import com.ordertracking.trackingservice.infrastructure.filter.MdcWebFilter;
import com.ordertracking.trackingservice.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(TrackingController.class)
@Import({MdcWebFilter.class, GlobalExceptionHandler.class})
class TrackingControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    OrderTrackingService orderTrackingService;

    @Test
    void shouldReturnTrackingDtoForKnownOrderId() {
        OrderTrackingDto dto = new OrderTrackingDto(
            "order-1", "cust-1", "RECEIVED",
            BigDecimal.TEN, Collections.emptyList(), Collections.emptyList(),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z")
        );
        when(orderTrackingService.getTracking(eq("order-1"))).thenReturn(Mono.just(dto));

        webTestClient.get()
            .uri("/api/v1/tracking/order-1")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.orderId").isEqualTo("order-1")
            .jsonPath("$.customerId").isEqualTo("cust-1")
            .jsonPath("$.status").isEqualTo("RECEIVED");
    }

    @Test
    void shouldReturn404WhenOrderNotFound() {
        when(orderTrackingService.getTracking(eq("unknown")))
            .thenReturn(Mono.error(new NotFoundException("Tracking not found for orderId: unknown")));

        webTestClient.get()
            .uri("/api/v1/tracking/unknown")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnJsonContentType() {
        OrderTrackingDto dto = new OrderTrackingDto(
            "order-2", "cust-2", "SHIPPED",
            BigDecimal.ONE, Collections.emptyList(), Collections.emptyList(),
            Instant.now(), Instant.now()
        );
        when(orderTrackingService.getTracking(eq("order-2"))).thenReturn(Mono.just(dto));

        webTestClient.get()
            .uri("/api/v1/tracking/order-2")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);
    }

    @Test
    void shouldPropagateCorrelationHeadersInResponse() {
        OrderTrackingDto dto = new OrderTrackingDto(
            "order-3", "cust-3", "DELIVERED",
            BigDecimal.TEN, Collections.emptyList(), Collections.emptyList(),
            Instant.now(), Instant.now()
        );
        when(orderTrackingService.getTracking(eq("order-3"))).thenReturn(Mono.just(dto));

        webTestClient.get()
            .uri("/api/v1/tracking/order-3")
            .header("X-Request-Id", "req-abc")
            .header("X-Trace-Id", "trace-xyz")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("X-Request-Id", "req-abc")
            .expectHeader().valueEquals("X-Trace-Id", "trace-xyz");
    }
}

