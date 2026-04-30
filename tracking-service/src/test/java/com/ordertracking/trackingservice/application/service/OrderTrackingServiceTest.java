package com.ordertracking.trackingservice.application.service;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.domain.exception.NotFoundException;
import com.ordertracking.trackingservice.domain.model.OrderTracking;
import com.ordertracking.trackingservice.domain.model.TrackingStatus;
import com.ordertracking.trackingservice.domain.repository.OrderTrackingRepository;
import com.ordertracking.trackingservice.infrastructure.persistence.mapper.OrderTrackingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTrackingServiceTest {

    @Mock OrderTrackingRepository orderTrackingRepository;
    @Mock ReactiveRedisTemplate<String, OrderTrackingDto> redisTemplate;
    @Mock ReactiveValueOperations<String, OrderTrackingDto> valueOperations;

    OrderTrackingMapper mapper = new OrderTrackingMapper();
    OrderTrackingService service;

    @BeforeEach
    void setup() {
        service = new OrderTrackingService(orderTrackingRepository, redisTemplate, mapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnTrackingFromCache() {
        OrderTrackingDto cachedDto = new OrderTrackingDto("order-1", "cust-1", "PENDING",
            BigDecimal.TEN, Collections.emptyList(), Instant.now(), Instant.now());
        when(valueOperations.get("tracking:order-1")).thenReturn(Mono.just(cachedDto));

        StepVerifier.create(service.getTracking("order-1"))
            .expectNextMatches(dto -> "order-1".equals(dto.orderId()))
            .verifyComplete();
    }

    @Test
    void shouldFallbackToDbOnCacheMiss() {
        when(valueOperations.get("tracking:order-1")).thenReturn(Mono.empty());

        OrderTracking domain = new OrderTracking();
        domain.setOrderId("order-1");
        domain.setCustomerId("cust-1");
        domain.setStatus(TrackingStatus.PENDING);
        domain.setTotalAmount(BigDecimal.TEN);
        domain.setCreatedAt(Instant.now());
        domain.setUpdatedAt(Instant.now());
        domain.setItems(Collections.emptyList());

        when(orderTrackingRepository.findByOrderId("order-1")).thenReturn(Mono.just(domain));
        when(valueOperations.set(eq("tracking:order-1"), any(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(service.getTracking("order-1"))
            .expectNextMatches(dto -> "order-1".equals(dto.orderId()))
            .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundErrorWhenNotFoundAnywhere() {
        when(valueOperations.get("tracking:order-x")).thenReturn(Mono.empty());
        when(orderTrackingRepository.findByOrderId("order-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.getTracking("order-x"))
            .expectErrorMatches(ex -> ex instanceof NotFoundException
                && ex.getMessage().contains("order-x"))
            .verify();
    }

    @Test
    void shouldUpdateTrackingStatusAndInvalidateCache() {
        OrderTracking existing = new OrderTracking();
        existing.setId("doc-1");
        existing.setOrderId("order-2");
        existing.setCustomerId("cust-2");
        existing.setStatus(TrackingStatus.PENDING);
        existing.setTotalAmount(BigDecimal.TEN);
        existing.setCreatedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());
        existing.setItems(Collections.emptyList());

        OrderTracking saved = new OrderTracking();
        saved.setId("doc-1");
        saved.setOrderId("order-2");
        saved.setCustomerId("cust-2");
        saved.setStatus(TrackingStatus.SHIPPED);
        saved.setTotalAmount(BigDecimal.TEN);
        saved.setCreatedAt(existing.getCreatedAt());
        saved.setUpdatedAt(Instant.now());
        saved.setItems(Collections.emptyList());

        when(orderTrackingRepository.findByOrderId("order-2")).thenReturn(Mono.just(existing));
        when(orderTrackingRepository.save(any())).thenReturn(Mono.just(saved));
        when(redisTemplate.delete(eq("tracking:order-2"))).thenReturn(Mono.just(1L));
        when(valueOperations.set(eq("tracking:order-2"), any(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateTrackingStatus("order-2", "SHIPPED"))
            .expectNextMatches(dto -> "SHIPPED".equals(dto.status()) && "order-2".equals(dto.orderId()))
            .verifyComplete();

        verify(redisTemplate).delete("tracking:order-2");
        verify(valueOperations).set(eq("tracking:order-2"), any(), any());
    }

    @Test
    void shouldReturnEmptyWhenUpdatingStatusForMissingOrder() {
        when(orderTrackingRepository.findByOrderId("order-missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.updateTrackingStatus("order-missing", "SHIPPED"))
            .verifyComplete();
    }
}
