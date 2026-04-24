package com.ordertracking.trackingservice.infrastructure.filter;

import com.ordertracking.common.mdc.MdcConstants;
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
 * WebFlux filter that propagates correlation IDs via HTTP response headers and Reactor Context,
 * and lifts them into MDC on each reactive signal via {@code doOnEach}.
 *
 * <p>In WebFlux, a single event-loop thread can interleave <em>multiple</em> concurrent requests,
 * so setting thread-local MDC once at filter entry is unsafe — it can bleed into other requests
 * that share the same thread.  The safe approach is to:</p>
 * <ol>
 *   <li>Store correlation IDs in the per-subscription <strong>Reactor Context</strong>
 *       (via {@code contextWrite}) — this is request-scoped, not thread-scoped.</li>
 *   <li>Set MDC <em>per signal</em> by reading from the context in {@code doOnEach},
 *       so any log statement executed in a signal callback sees the correct IDs.</li>
 * </ol>
 */
@Component
@Order(-1)
public class MdcWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(MdcConstants.HEADER_REQUEST_ID))
            .orElse(UUID.randomUUID().toString().substring(0, 8));
        String traceId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(MdcConstants.HEADER_TRACE_ID))
            .orElse(UUID.randomUUID().toString());
        String userId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(MdcConstants.HEADER_USER_ID))
            .orElse("anon");

        exchange.getResponse().getHeaders().set(MdcConstants.HEADER_REQUEST_ID, requestId);
        exchange.getResponse().getHeaders().set(MdcConstants.HEADER_TRACE_ID, traceId);

        return chain.filter(exchange)
            // Store in Reactor Context so downstream operators can access them safely,
            // even when execution has moved to a different thread.
            .contextWrite(ctx -> ctx
                .put(MdcConstants.REQUEST_ID, requestId)
                .put(MdcConstants.TRACE_ID,   traceId)
                .put(MdcConstants.USER_ID,    userId)
            )
            // Lift context values into MDC per-signal so that log statements inside
            // signal callbacks (doOnNext, doOnError, doOnComplete) carry the correct IDs.
            // This avoids the risk of one request's MDC polluting another request's log
            // lines when they share an event-loop thread.
            .doOnEach(signal -> {
                reactor.util.context.ContextView ctx = signal.getContextView();
                ctx.getOrEmpty(MdcConstants.TRACE_ID).ifPresent(v -> MDC.put(MdcConstants.TRACE_ID, v.toString()));
                ctx.getOrEmpty(MdcConstants.REQUEST_ID).ifPresent(v -> MDC.put(MdcConstants.REQUEST_ID, v.toString()));
                ctx.getOrEmpty(MdcConstants.USER_ID).ifPresent(v -> MDC.put(MdcConstants.USER_ID, v.toString()));
                if (signal.isOnComplete() || signal.isOnError()) {
                    MDC.clear();
                }
            });
    }
}
