package com.ordertracking.orderservice.infrastructure.filter;

import com.ordertracking.common.mdc.MdcConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcRequestFilter extends OncePerRequestFilter {

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
            MDC.clear(); // CRITICAL for thread-pool reuse
        }
    }

    private void populateMdc(HttpServletRequest req) {
        String requestId = Optional.ofNullable(req.getHeader(MdcConstants.HEADER_REQUEST_ID))
            .orElse(UUID.randomUUID().toString().substring(0, 8));
        String traceId = Optional.ofNullable(req.getHeader(MdcConstants.HEADER_TRACE_ID))
            .orElse(UUID.randomUUID().toString());
        MDC.put(MdcConstants.REQUEST_ID, requestId);
        MDC.put(MdcConstants.TRACE_ID,   traceId);
        MDC.put(MdcConstants.USER_ID,    Optional.ofNullable(req.getHeader(MdcConstants.HEADER_USER_ID)).orElse("anon"));
        MDC.put(MdcConstants.METHOD,     req.getMethod());
        MDC.put(MdcConstants.URI,        req.getRequestURI());
        MDC.put(MdcConstants.SERVICE,    serviceName);
        MDC.put(MdcConstants.CLIENT_IP,  req.getRemoteAddr());
    }

    private void propagateToResponse(HttpServletResponse res) {
        res.setHeader(MdcConstants.HEADER_REQUEST_ID, MDC.get(MdcConstants.REQUEST_ID));
        res.setHeader(MdcConstants.HEADER_TRACE_ID,   MDC.get(MdcConstants.TRACE_ID));
    }
}
