package com.ordertracking.trackingservice.application.service;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.domain.repository.OrderTrackingRepository;
import com.ordertracking.trackingservice.infrastructure.persistence.mapper.OrderTrackingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class OrderTrackingService {
    private static final Logger log = LoggerFactory.getLogger(OrderTrackingService.class);

    private final OrderTrackingRepository orderTrackingRepository;
    private final ReactiveRedisTemplate<String, OrderTrackingDto> redisTemplate;
    private final OrderTrackingMapper mapper;

    @Value("${tracking.cache.ttl-seconds:300}")
    private long cacheTtl;

    public OrderTrackingService(OrderTrackingRepository orderTrackingRepository,
                                 ReactiveRedisTemplate<String, OrderTrackingDto> redisTemplate,
                                 OrderTrackingMapper mapper) {
        this.orderTrackingRepository = orderTrackingRepository;
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    public Mono<OrderTrackingDto> getOrderTracking(String orderId) {
        String cacheKey = cacheKey(orderId);
        Mono<OrderTrackingDto> dbFallback = Mono.defer(() ->
            orderTrackingRepository.findByOrderId(orderId)
                .map(mapper::toDto)
                .flatMap(dto ->
                    redisTemplate.opsForValue()
                        .set(cacheKey, dto, Duration.ofSeconds(cacheTtl))
                        .thenReturn(dto)
                )
                .doOnNext(dto -> log.debug("Loaded from DB and cached for orderId={}", orderId))
        );

        return redisTemplate.opsForValue().get(cacheKey)
            .doOnNext(dto -> log.debug("Cache hit for orderId={}", orderId))
            // Mono.defer ensures findByOrderId is only called (and subscribed to)
            // when the cache is actually empty — not during chain assembly.
            .switchIfEmpty(dbFallback)
            // If Redis itself throws an error (connection refused, timeout, etc.)
            // fall back transparently to MongoDB so the caller still gets a result.
            .onErrorResume(ex -> {
                log.warn("Redis error for orderId={}, falling back to DB: {}", orderId, ex.getMessage());
                return dbFallback;
            });
    }

    public Mono<OrderTrackingDto> saveTracking(OrderTrackingDto dto) {
        return orderTrackingRepository.save(mapper.fromDto(dto))
            .map(mapper::toDto)
            .flatMap(saved -> {
                String cacheKey = cacheKey(saved.orderId());
                // Evict any stale entry first, then write the fresh value.
                return redisTemplate.delete(cacheKey)
                    .then(redisTemplate.opsForValue()
                        .set(cacheKey, saved, Duration.ofSeconds(cacheTtl)))
                    .thenReturn(saved);
            });
    }

    private static String cacheKey(String orderId) {
        return "tracking:" + orderId;
    }
}
