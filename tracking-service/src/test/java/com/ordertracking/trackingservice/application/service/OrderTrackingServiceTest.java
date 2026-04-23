package com.ordertracking.trackingservice.application.service;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
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
    @Mock ReactiveRedisTemplate<String, Object> redisTemplate;
    @Mock ReactiveValueOperations<String, Object> valueOperations;

    OrderTrackingMapper mapper = new OrderTrackingMapper();
    OrderTrackingService service;

    @BeforeEach
    void setup() {
        service = new OrderTrackingService(orderTrackingRepository, redisTemplate, mapper);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnTrackingFromCache() {
        OrderTrackingDto cachedDto = new OrderTrackingDto("order-1", "cust-1", "PENDING",
            BigDecimal.TEN, Collections.emptyList(), Instant.now(), Instant.now());
        when(valueOperations.get("tracking:order-1")).thenReturn(Mono.just(cachedDto));

        StepVerifier.create(service.getOrderTracking("order-1"))
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

        StepVerifier.create(service.getOrderTracking("order-1"))
            .expectNextMatches(dto -> "order-1".equals(dto.orderId()))
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNotFoundAnywhere() {
        when(valueOperations.get("tracking:order-x")).thenReturn(Mono.empty());
        when(orderTrackingRepository.findByOrderId("order-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.getOrderTracking("order-x"))
            .verifyComplete();
    }
}
