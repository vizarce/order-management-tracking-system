package com.ordertracking.trackingservice.infrastructure.filter;

import com.ordertracking.common.mdc.MdcConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.assertThat;

class MdcWebFilterTest {

    private final MdcWebFilter filter = new MdcWebFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPropagateRequestHeadersToReactorContext() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header(MdcConstants.HEADER_REQUEST_ID, "req-123")
                .header(MdcConstants.HEADER_TRACE_ID, "trace-456")
                .header(MdcConstants.HEADER_USER_ID, "user-789")
                .build()
        );

        AtomicReference<String> capturedRequestId = new AtomicReference<>();
        AtomicReference<String> capturedTraceId   = new AtomicReference<>();
        AtomicReference<String> capturedUserId    = new AtomicReference<>();

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            capturedRequestId.set(ctx.getOrDefault(MdcConstants.REQUEST_ID, null));
            capturedTraceId.set(ctx.getOrDefault(MdcConstants.TRACE_ID, null));
            capturedUserId.set(ctx.getOrDefault(MdcConstants.USER_ID, null));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        assertThat(capturedRequestId.get()).isEqualTo("req-123");
        assertThat(capturedTraceId.get()).isEqualTo("trace-456");
        assertThat(capturedUserId.get()).isEqualTo("user-789");
    }

    @Test
    void shouldGenerateRandomIdsWhenHeadersAreMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test").build()
        );

        AtomicReference<String> capturedRequestId = new AtomicReference<>();
        AtomicReference<String> capturedTraceId   = new AtomicReference<>();
        AtomicReference<String> capturedUserId    = new AtomicReference<>();

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            capturedRequestId.set(ctx.getOrDefault(MdcConstants.REQUEST_ID, null));
            capturedTraceId.set(ctx.getOrDefault(MdcConstants.TRACE_ID, null));
            capturedUserId.set(ctx.getOrDefault(MdcConstants.USER_ID, null));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        assertThat(capturedRequestId.get()).isNotNull().isNotEmpty();
        assertThat(capturedTraceId.get()).isNotNull().isNotEmpty();
        assertThat(capturedUserId.get()).isEqualTo("anon");
    }

    @Test
    void shouldEchoCorrelationIdsInResponseHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header(MdcConstants.HEADER_REQUEST_ID, "req-111")
                .header(MdcConstants.HEADER_TRACE_ID, "trace-222")
                .build()
        );

        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(MdcConstants.HEADER_REQUEST_ID))
            .isEqualTo("req-111");
        assertThat(exchange.getResponse().getHeaders().getFirst(MdcConstants.HEADER_TRACE_ID))
            .isEqualTo("trace-222");
    }

    @Test
    void shouldClearMdcAfterError() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header(MdcConstants.HEADER_REQUEST_ID, "req-999")
                .header(MdcConstants.HEADER_TRACE_ID, "trace-888")
                .build()
        );

        WebFilterChain chain = ex -> Mono.error(new RuntimeException("downstream error"));

        StepVerifier.create(filter.filter(exchange, chain))
            .expectError(RuntimeException.class)
            .verify();

        // doOnEach fires on onError: sets MDC then clears it, leaving MDC clean
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void shouldClearMdcOnCompletion() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header(MdcConstants.HEADER_REQUEST_ID, "req-clear")
                .build()
        );

        WebFilterChain chain = ex -> Mono.empty();

        // After the reactive chain completes, doOnEach fires isOnComplete and clears MDC.
        // We verify via StepVerifier that the chain runs to completion without errors.
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        // MDC should be clear after the signal-level MDC.clear() call on completion.
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
