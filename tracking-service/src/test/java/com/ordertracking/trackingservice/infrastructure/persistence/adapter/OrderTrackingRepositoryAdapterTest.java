package com.ordertracking.trackingservice.infrastructure.persistence.adapter;

import com.ordertracking.trackingservice.domain.model.OrderTracking;
import com.ordertracking.trackingservice.domain.model.TrackingStatus;
import com.ordertracking.trackingservice.infrastructure.persistence.document.OrderTrackingDocument;
import com.ordertracking.trackingservice.infrastructure.persistence.mapper.OrderTrackingMapper;
import com.ordertracking.trackingservice.infrastructure.persistence.repository.ReactiveOrderTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTrackingRepositoryAdapterTest {

    @Mock
    ReactiveOrderTrackingRepository reactiveRepo;

    OrderTrackingMapper mapper = new OrderTrackingMapper();
    OrderTrackingRepositoryAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new OrderTrackingRepositoryAdapter(reactiveRepo, mapper);
    }

    @Test
    void save_persistsDocumentAndReturnsDomain() {
        OrderTracking input = buildDomain("order-save-1");
        OrderTrackingDocument savedDoc = mapper.toDocument(input);
        savedDoc.setId("generated-id");

        when(reactiveRepo.save(any(OrderTrackingDocument.class))).thenReturn(Mono.just(savedDoc));

        StepVerifier.create(adapter.save(input))
            .expectNextMatches(result ->
                "order-save-1".equals(result.getOrderId()) &&
                "cust-1".equals(result.getCustomerId()) &&
                TrackingStatus.PENDING == result.getStatus()
            )
            .verifyComplete();

        verify(reactiveRepo).save(any(OrderTrackingDocument.class));
    }

    @Test
    void findByOrderId_returnsMatchingDomain() {
        OrderTrackingDocument doc = buildDocument("order-find-1");
        when(reactiveRepo.findByOrderId("order-find-1")).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.findByOrderId("order-find-1"))
            .expectNextMatches(result ->
                "order-find-1".equals(result.getOrderId()) &&
                TrackingStatus.PROCESSING == result.getStatus()
            )
            .verifyComplete();
    }

    @Test
    void findByOrderId_returnsEmptyWhenNotFound() {
        when(reactiveRepo.findByOrderId("order-missing")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByOrderId("order-missing"))
            .verifyComplete();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private OrderTracking buildDomain(String orderId) {
        OrderTracking domain = new OrderTracking();
        domain.setOrderId(orderId);
        domain.setCustomerId("cust-1");
        domain.setStatus(TrackingStatus.PENDING);
        domain.setTotalAmount(new BigDecimal("50.00"));
        domain.setCreatedAt(Instant.now());
        domain.setUpdatedAt(Instant.now());
        domain.setItems(Collections.emptyList());
        return domain;
    }

    private OrderTrackingDocument buildDocument(String orderId) {
        OrderTrackingDocument doc = new OrderTrackingDocument();
        doc.setId("doc-id");
        doc.setOrderId(orderId);
        doc.setCustomerId("cust-1");
        doc.setStatus("PROCESSING");
        doc.setTotalAmount(new BigDecimal("75.00"));
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        doc.setItems(Collections.emptyList());
        return doc;
    }
}
