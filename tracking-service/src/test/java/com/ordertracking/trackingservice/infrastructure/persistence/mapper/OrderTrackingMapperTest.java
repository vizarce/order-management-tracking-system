package com.ordertracking.trackingservice.infrastructure.persistence.mapper;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.domain.model.OrderTracking;
import com.ordertracking.trackingservice.domain.model.TrackingStatus;
import com.ordertracking.trackingservice.infrastructure.persistence.document.OrderTrackingDocument;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTrackingMapperTest {

    private final OrderTrackingMapper mapper = new OrderTrackingMapper();

    // ── toDocument ────────────────────────────────────────────────────────────

    @Test
    void toDocument_mapsAllFields() {
        OrderTracking domain = buildDomain("ord-1", "cust-1", TrackingStatus.SHIPPED);

        OrderTrackingDocument doc = mapper.toDocument(domain);

        assertThat(doc.getId()).isEqualTo("doc-id");
        assertThat(doc.getOrderId()).isEqualTo("ord-1");
        assertThat(doc.getCustomerId()).isEqualTo("cust-1");
        assertThat(doc.getStatus()).isEqualTo("SHIPPED");
        assertThat(doc.getTotalAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(doc.getItems()).hasSize(1);
        assertThat(doc.getItems().get(0).getProductId()).isEqualTo("prod-1");
        assertThat(doc.getItems().get(0).getProductName()).isEqualTo("Widget");
        assertThat(doc.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(doc.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("49.99"));
    }

    @Test
    void toDocument_nullStatusAndItems_areHandledGracefully() {
        OrderTracking domain = new OrderTracking();
        domain.setOrderId("ord-2");

        OrderTrackingDocument doc = mapper.toDocument(domain);

        assertThat(doc.getStatus()).isNull();
        assertThat(doc.getItems()).isNull();
    }

    // ── toDomain ──────────────────────────────────────────────────────────────

    @Test
    void toDomain_mapsAllFields() {
        OrderTrackingDocument doc = buildDocument("ord-3", "cust-3", "DELIVERED");

        OrderTracking domain = mapper.toDomain(doc);

        assertThat(domain.getOrderId()).isEqualTo("ord-3");
        assertThat(domain.getCustomerId()).isEqualTo("cust-3");
        assertThat(domain.getStatus()).isEqualTo(TrackingStatus.DELIVERED);
        assertThat(domain.getTotalAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(domain.getItems()).hasSize(1);
        assertThat(domain.getItems().get(0).getProductId()).isEqualTo("prod-1");
    }

    @Test
    void toDomain_nullStatusAndItems_areHandledGracefully() {
        OrderTrackingDocument doc = new OrderTrackingDocument();
        doc.setOrderId("ord-4");

        OrderTracking domain = mapper.toDomain(doc);

        assertThat(domain.getStatus()).isNull();
        assertThat(domain.getItems()).isNull();
    }

    // ── toDto ─────────────────────────────────────────────────────────────────

    @Test
    void toDto_mapsAllFields() {
        OrderTracking domain = buildDomain("ord-5", "cust-5", TrackingStatus.PROCESSING);

        OrderTrackingDto dto = mapper.toDto(domain);

        assertThat(dto.orderId()).isEqualTo("ord-5");
        assertThat(dto.customerId()).isEqualTo("cust-5");
        assertThat(dto.status()).isEqualTo("PROCESSING");
        assertThat(dto.totalAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().get(0).productId()).isEqualTo("prod-1");
    }

    @Test
    void toDto_nullStatusYieldsNullStatusString() {
        OrderTracking domain = new OrderTracking();
        domain.setOrderId("ord-6");
        domain.setItems(List.of());

        OrderTrackingDto dto = mapper.toDto(domain);

        assertThat(dto.status()).isNull();
        assertThat(dto.items()).isEmpty();
    }

    // ── fromDto ───────────────────────────────────────────────────────────────

    @Test
    void fromDto_mapsAllFields() {
        Instant now = Instant.now();
        OrderTrackingDto dto = new OrderTrackingDto(
            "ord-7", "cust-7", "RECEIVED", new BigDecimal("10.00"),
            List.of(new OrderTrackingDto.TrackingItemDto("prod-7", "Gadget", 1, new BigDecimal("10.00"))),
            List.of(new OrderTrackingDto.TrackingEventDto(now, "RECEIVED", "Order received")),
            now, now
        );

        OrderTracking domain = mapper.fromDto(dto);

        assertThat(domain.getOrderId()).isEqualTo("ord-7");
        assertThat(domain.getCustomerId()).isEqualTo("cust-7");
        assertThat(domain.getStatus()).isEqualTo(TrackingStatus.RECEIVED);
        assertThat(domain.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(domain.getItems()).hasSize(1);
        assertThat(domain.getItems().get(0).getProductId()).isEqualTo("prod-7");
        assertThat(domain.getEventLog()).hasSize(1);
        assertThat(domain.getEventLog().get(0).getStatus()).isEqualTo(TrackingStatus.RECEIVED);
        assertThat(domain.getEventLog().get(0).getDescription()).isEqualTo("Order received");
    }

    @Test
    void fromDto_nullStatusYieldsNullDomainStatus() {
        OrderTrackingDto dto = new OrderTrackingDto("ord-8", "cust-8", null,
            BigDecimal.ZERO, List.of(), null, Instant.now(), Instant.now());

        OrderTracking domain = mapper.fromDto(dto);

        assertThat(domain.getStatus()).isNull();
    }

    // ── round-trip ────────────────────────────────────────────────────────────

    @Test
    void documentRoundTrip_domainToDocumentAndBack_preservesData() {
        OrderTracking original = buildDomain("ord-9", "cust-9", TrackingStatus.FAILED);

        OrderTracking restored = mapper.toDomain(mapper.toDocument(original));

        assertThat(restored.getOrderId()).isEqualTo(original.getOrderId());
        assertThat(restored.getCustomerId()).isEqualTo(original.getCustomerId());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getTotalAmount()).isEqualByComparingTo(original.getTotalAmount());
        assertThat(restored.getItems()).hasSize(1);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private OrderTracking buildDomain(String orderId, String customerId, TrackingStatus status) {
        Instant now = Instant.now();
        OrderTracking domain = new OrderTracking();
        domain.setId("doc-id");
        domain.setOrderId(orderId);
        domain.setCustomerId(customerId);
        domain.setStatus(status);
        domain.setTotalAmount(new BigDecimal("99.99"));
        domain.setCreatedAt(now);
        domain.setUpdatedAt(now);
        domain.setItems(List.of(
            new OrderTracking.TrackingItem("prod-1", "Widget", 2, new BigDecimal("49.99"))
        ));
        return domain;
    }

    private OrderTrackingDocument buildDocument(String orderId, String customerId, String status) {
        Instant now = Instant.now();
        OrderTrackingDocument doc = new OrderTrackingDocument();
        doc.setId("doc-id");
        doc.setOrderId(orderId);
        doc.setCustomerId(customerId);
        doc.setStatus(status);
        doc.setTotalAmount(new BigDecimal("99.99"));
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        OrderTrackingDocument.TrackingItemDoc item = new OrderTrackingDocument.TrackingItemDoc();
        item.setProductId("prod-1");
        item.setProductName("Widget");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("49.99"));
        doc.setItems(List.of(item));
        return doc;
    }
}
