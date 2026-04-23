package com.ordertracking.trackingservice.infrastructure.filter;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(-1)
public class MdcWebFilter implements WebFilter {
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_TRACE_ID   = "X-Trace-Id";
    private static final String HEADER_USER_ID    = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID))
            .orElse(UUID.randomUUID().toString().substring(0, 8));
        String traceId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER_TRACE_ID))
            .orElse(UUID.randomUUID().toString());
        String userId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID))
            .orElse("anon");

        exchange.getResponse().getHeaders().set(HEADER_REQUEST_ID, requestId);
        exchange.getResponse().getHeaders().set(HEADER_TRACE_ID, traceId);

        return chain.filter(exchange)
            .contextWrite(ctx -> ctx
                .put("requestId", requestId)
                .put("traceId", traceId)
                .put("userId", userId)
            );
    }
}
