package com.ordertracking.trackingservice.infrastructure.filter;

import com.ordertracking.common.mdc.MdcConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcWebFilterTest {

    private MdcWebFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MdcWebFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void populatesReactorContextFromIncomingHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
            .header(MdcConstants.HEADER_TRACE_ID,   "trace-from-header")
            .header(MdcConstants.HEADER_REQUEST_ID, "req-from-header")
            .header(MdcConstants.HEADER_USER_ID,    "user-from-header")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> capturedTrace   = new AtomicReference<>();
        AtomicReference<String> capturedRequest = new AtomicReference<>();
        AtomicReference<String> capturedUser    = new AtomicReference<>();

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            capturedTrace.set(ctx.getOrDefault(MdcConstants.TRACE_ID,   null));
            capturedRequest.set(ctx.getOrDefault(MdcConstants.REQUEST_ID, null));
            capturedUser.set(ctx.getOrDefault(MdcConstants.USER_ID,      null));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        assertThat(capturedTrace.get()).isEqualTo("trace-from-header");
        assertThat(capturedRequest.get()).isEqualTo("req-from-header");
        assertThat(capturedUser.get()).isEqualTo("user-from-header");
    }

    @Test
    void generatesCorrelationIdsWhenHeadersAbsent() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> capturedTrace   = new AtomicReference<>();
        AtomicReference<String> capturedRequest = new AtomicReference<>();
        AtomicReference<String> capturedUser    = new AtomicReference<>();

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            capturedTrace.set(ctx.getOrDefault(MdcConstants.TRACE_ID,   null));
            capturedRequest.set(ctx.getOrDefault(MdcConstants.REQUEST_ID, null));
            capturedUser.set(ctx.getOrDefault(MdcConstants.USER_ID,      null));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        assertThat(capturedTrace.get()).isNotNull().isNotBlank();
        assertThat(capturedRequest.get()).isNotNull().isNotBlank();
        assertThat(capturedUser.get()).isEqualTo("anon");
    }

    @Test
    void setsCorrelationIdsInResponseHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
            .header(MdcConstants.HEADER_TRACE_ID,   "t-resp")
            .header(MdcConstants.HEADER_REQUEST_ID, "r-resp")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertThat(responseHeaders.getFirst(MdcConstants.HEADER_TRACE_ID)).isEqualTo("t-resp");
        assertThat(responseHeaders.getFirst(MdcConstants.HEADER_REQUEST_ID)).isEqualTo("r-resp");
    }

    @Test
    void signalsCarryEnrichedContextForDownstreamObservers() {
        // Verify that the Reactor Context written by the filter is accessible to
        // upstream operators, confirming that signals carry the enriched context
        // with all tracing IDs needed for logging and observability.
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
            .header(MdcConstants.HEADER_TRACE_ID,   "signal-trace")
            .header(MdcConstants.HEADER_REQUEST_ID, "signal-req")
            .header(MdcConstants.HEADER_USER_ID,    "signal-user")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
            .expectAccessibleContext()
            .contains(MdcConstants.TRACE_ID,   "signal-trace")
            .contains(MdcConstants.REQUEST_ID, "signal-req")
            .contains(MdcConstants.USER_ID,    "signal-user")
            .then()
            .verifyComplete();
    }

    @Test
    void clearsMdcOnCompletion() {
        MDC.put(MdcConstants.TRACE_ID, "pre-existing");

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        assertThat(MDC.get(MdcConstants.TRACE_ID)).isNull();
    }
}
