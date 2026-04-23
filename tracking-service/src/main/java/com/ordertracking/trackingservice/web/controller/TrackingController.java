package com.ordertracking.trackingservice.web.controller;

import com.ordertracking.trackingservice.application.dto.OrderTrackingDto;
import com.ordertracking.trackingservice.application.service.OrderTrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {
    private final OrderTrackingService orderTrackingService;

    public TrackingController(OrderTrackingService orderTrackingService) {
        this.orderTrackingService = orderTrackingService;
    }

    @GetMapping("/{orderId}")
    public Mono<OrderTrackingDto> getTracking(@PathVariable String orderId) {
        return orderTrackingService.getOrderTracking(orderId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracking not found for orderId: " + orderId)));
    }
}
