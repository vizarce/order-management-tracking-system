package com.ordertracking.orderservice.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcRequestFilter extends OncePerRequestFilter {
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_TRACE_ID   = "X-Trace-Id";
    private static final String HEADER_USER_ID    = "X-User-Id";

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            populateMdc(request);
            propagateToResponse(response);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void populateMdc(HttpServletRequest req) {
        String requestId = Optional.ofNullable(req.getHeader(HEADER_REQUEST_ID))
            .orElse(UUID.randomUUID().toString().substring(0, 8));
        String traceId = Optional.ofNullable(req.getHeader(HEADER_TRACE_ID))
            .orElse(UUID.randomUUID().toString());
        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        MDC.put("userId", Optional.ofNullable(req.getHeader(HEADER_USER_ID)).orElse("anon"));
        MDC.put("method", req.getMethod());
        MDC.put("uri", req.getRequestURI());
        MDC.put("service", serviceName);
        MDC.put("clientIp", req.getRemoteAddr());
    }

    private void propagateToResponse(HttpServletResponse res) {
        res.setHeader(HEADER_REQUEST_ID, MDC.get("requestId"));
        res.setHeader(HEADER_TRACE_ID, MDC.get("traceId"));
    }
}
