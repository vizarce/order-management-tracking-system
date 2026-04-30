package com.ordertracking.orderservice.infrastructure.filter;

import com.ordertracking.common.mdc.MdcConstants;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcRequestFilterTest {

    private MdcRequestFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MdcRequestFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void populatesMdcFromIncomingHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MdcConstants.HEADER_TRACE_ID,   "my-trace-id");
        request.addHeader(MdcConstants.HEADER_REQUEST_ID, "my-request-id");
        request.addHeader(MdcConstants.HEADER_USER_ID,    "user-42");

        AtomicReference<String> capturedTrace   = new AtomicReference<>();
        AtomicReference<String> capturedRequest = new AtomicReference<>();
        AtomicReference<String> capturedUser    = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            capturedTrace.set(MDC.get(MdcConstants.TRACE_ID));
            capturedRequest.set(MDC.get(MdcConstants.REQUEST_ID));
            capturedUser.set(MDC.get(MdcConstants.USER_ID));
        };
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(capturedTrace.get()).isEqualTo("my-trace-id");
        assertThat(capturedRequest.get()).isEqualTo("my-request-id");
        assertThat(capturedUser.get()).isEqualTo("user-42");
    }

    @Test
    void generatesMdcIdsWhenHeadersAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        AtomicReference<String> capturedTrace   = new AtomicReference<>();
        AtomicReference<String> capturedRequest = new AtomicReference<>();
        AtomicReference<String> capturedUser    = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            capturedTrace.set(MDC.get(MdcConstants.TRACE_ID));
            capturedRequest.set(MDC.get(MdcConstants.REQUEST_ID));
            capturedUser.set(MDC.get(MdcConstants.USER_ID));
        };
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(capturedTrace.get()).isNotNull().isNotBlank();
        assertThat(capturedRequest.get()).isNotNull().isNotBlank();
        assertThat(capturedUser.get()).isEqualTo("anon");
    }

    @Test
    void clearsMdcAfterRequestCompletes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MdcConstants.HEADER_TRACE_ID, "trace-to-clear");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(MDC.get(MdcConstants.TRACE_ID)).isNull();
        assertThat(MDC.get(MdcConstants.REQUEST_ID)).isNull();
    }

    @Test
    void propagatesCorrelationIdsInResponseHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MdcConstants.HEADER_TRACE_ID,   "t-123");
        request.addHeader(MdcConstants.HEADER_REQUEST_ID, "r-456");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(MdcConstants.HEADER_TRACE_ID)).isEqualTo("t-123");
        assertThat(response.getHeader(MdcConstants.HEADER_REQUEST_ID)).isEqualTo("r-456");
    }

    @Test
    void populatesMethodUriAndClientIpInMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/orders");
        request.setRemoteAddr("10.0.0.1");

        AtomicReference<String> capturedMethod   = new AtomicReference<>();
        AtomicReference<String> capturedUri      = new AtomicReference<>();
        AtomicReference<String> capturedClientIp = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            capturedMethod.set(MDC.get(MdcConstants.METHOD));
            capturedUri.set(MDC.get(MdcConstants.URI));
            capturedClientIp.set(MDC.get(MdcConstants.CLIENT_IP));
        };
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(capturedMethod.get()).isEqualTo("POST");
        assertThat(capturedUri.get()).isEqualTo("/api/v1/orders");
        assertThat(capturedClientIp.get()).isEqualTo("10.0.0.1");
    }
}
