package com.ordertracking.orderservice.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tracking-service", url = "${tracking.service.url:http://localhost:8081}")
public interface TrackingServiceClient {
    @GetMapping("/api/v1/tracking/{orderId}")
    Object getTracking(@PathVariable("orderId") String orderId);
}
