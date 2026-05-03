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
        if (domain.getEventLog() != null) {
            List<OrderTrackingDocument.TrackingEventDoc> eventDocs = domain.getEventLog().stream().map(e -> {
                OrderTrackingDocument.TrackingEventDoc d = new OrderTrackingDocument.TrackingEventDoc();
                d.setTimestamp(e.getTimestamp());
                d.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
                d.setDescription(e.getDescription());
                return d;
            }).collect(Collectors.toList());
            doc.setEventLog(eventDocs);
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
        if (doc.getEventLog() != null) {
            List<OrderTracking.TrackingEvent> events = doc.getEventLog().stream().map(d -> {
                return new OrderTracking.TrackingEvent(
                    d.getTimestamp(),
                    d.getStatus() != null ? TrackingStatus.valueOf(d.getStatus()) : null,
                    d.getDescription()
                );
            }).collect(Collectors.toList());
            domain.setEventLog(events);
        }
        return domain;
    }

    public OrderTrackingDto toDto(OrderTracking domain) {
        List<OrderTrackingDto.TrackingItemDto> items = domain.getItems() != null
            ? domain.getItems().stream()
                .map(i -> new OrderTrackingDto.TrackingItemDto(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList())
            : Collections.emptyList();

        List<OrderTrackingDto.TrackingEventDto> eventLog = domain.getEventLog() != null
            ? domain.getEventLog().stream()
                .map(e -> new OrderTrackingDto.TrackingEventDto(
                    e.getTimestamp(),
                    e.getStatus() != null ? e.getStatus().name() : null,
                    e.getDescription()
                ))
                .collect(Collectors.toList())
            : Collections.emptyList();

        return new OrderTrackingDto(
            domain.getOrderId(),
            domain.getCustomerId(),
            domain.getStatus() != null ? domain.getStatus().name() : null,
            domain.getTotalAmount(),
            items,
            eventLog,
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
        if (dto.eventLog() != null) {
            List<OrderTracking.TrackingEvent> events = dto.eventLog().stream()
                .map(e -> new OrderTracking.TrackingEvent(
                    e.timestamp(),
                    e.status() != null ? TrackingStatus.valueOf(e.status()) : null,
                    e.description()
                ))
                .collect(Collectors.toList());
            domain.setEventLog(events);
        }
        return domain;
    }
}
