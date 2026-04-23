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

/**
 * WebFlux filter that propagates correlation IDs via both HTTP response headers and Reactor Context.
 *
 * <p>In WebFlux, MDC values cannot be set directly on thread-locals since reactive chains
 * may execute across multiple threads. Correlation IDs are stored in the Reactor Context and
 * can be accessed in reactive operators via {@code Mono.deferContextual} or
 * {@code contextWrite}. For true MDC propagation in log statements, enable
 * {@code Hooks.enableAutomaticContextPropagation()} with a Micrometer context-propagation
 * bridge (e.g. {@code micrometer-context-propagation} on the classpath).</p>
 */
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

        // Populate MDC for the current thread (synchronous portion of the chain).
        // Downstream reactive operators should read from Reactor Context when off-thread.
        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        MDC.put("userId", userId);

        return chain.filter(exchange)
            .contextWrite(ctx -> ctx
                .put("requestId", requestId)
                .put("traceId", traceId)
                .put("userId", userId)
            )
            .doFinally(signal -> MDC.clear());
    }
}
