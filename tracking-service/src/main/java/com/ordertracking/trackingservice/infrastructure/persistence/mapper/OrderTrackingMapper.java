package com.ordertracking.trackingservice.infrastructure.persistence.mapper;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.domain.model.OrderTracking;
import com.ordertracking.trackingservice.domain.model.TrackingStatus;
import com.ordertracking.trackingservice.infrastructure.persistence.document.OrderTrackingDocument;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderTrackingMapper {

    public OrderTrackingDocument toDocument(OrderTracking domain) {
        OrderTrackingDocument doc = new OrderTrackingDocument();
        doc.setId(domain.getId());
        doc.setOrderId(domain.getOrderId());
        doc.setCustomerId(domain.getCustomerId());
        doc.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        doc.setTotalAmount(domain.getTotalAmount());
        doc.setCreatedAt(domain.getCreatedAt());
        doc.setUpdatedAt(domain.getUpdatedAt());
        if (domain.getItems() != null) {
            List<OrderTrackingDocument.TrackingItemDoc> itemDocs = domain.getItems().stream().map(i -> {
                OrderTrackingDocument.TrackingItemDoc d = new OrderTrackingDocument.TrackingItemDoc();
                d.setProductId(i.getProductId());
                d.setProductName(i.getProductName());
                d.setQuantity(i.getQuantity());
                d.setUnitPrice(i.getUnitPrice());
                return d;
            }).collect(Collectors.toList());
            doc.setItems(itemDocs);
        }
        return doc;
    }

    public OrderTracking toDomain(OrderTrackingDocument doc) {
        OrderTracking domain = new OrderTracking();
        domain.setId(doc.getId());
        domain.setOrderId(doc.getOrderId());
        domain.setCustomerId(doc.getCustomerId());
        domain.setStatus(doc.getStatus() != null ? TrackingStatus.valueOf(doc.getStatus()) : null);
        domain.setTotalAmount(doc.getTotalAmount());
        domain.setCreatedAt(doc.getCreatedAt());
        domain.setUpdatedAt(doc.getUpdatedAt());
        if (doc.getItems() != null) {
            List<OrderTracking.TrackingItem> items = doc.getItems().stream().map(d -> {
                return new OrderTracking.TrackingItem(d.getProductId(), d.getProductName(), d.getQuantity(), d.getUnitPrice());
            }).collect(Collectors.toList());
            domain.setItems(items);
        }
        return domain;
    }

    public OrderTrackingDto toDto(OrderTracking domain) {
        List<OrderTrackingDto.TrackingItemDto> items = domain.getItems() != null
            ? domain.getItems().stream()
                .map(i -> new OrderTrackingDto.TrackingItemDto(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList())
            : Collections.emptyList();

        return new OrderTrackingDto(
            domain.getOrderId(),
            domain.getCustomerId(),
            domain.getStatus() != null ? domain.getStatus().name() : null,
            domain.getTotalAmount(),
            items,
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }

    public OrderTracking fromDto(OrderTrackingDto dto) {
        OrderTracking domain = new OrderTracking();
        domain.setOrderId(dto.orderId());
        domain.setCustomerId(dto.customerId());
        domain.setStatus(dto.status() != null ? TrackingStatus.valueOf(dto.status()) : null);
        domain.setTotalAmount(dto.totalAmount());
        domain.setCreatedAt(dto.createdAt() != null ? dto.createdAt() : Instant.now());
        domain.setUpdatedAt(Instant.now());
        if (dto.items() != null) {
            List<OrderTracking.TrackingItem> items = dto.items().stream()
                .map(i -> new OrderTracking.TrackingItem(i.productId(), i.productName(), i.quantity(), i.unitPrice()))
                .collect(Collectors.toList());
            domain.setItems(items);
        }
        return domain;
    }
}
