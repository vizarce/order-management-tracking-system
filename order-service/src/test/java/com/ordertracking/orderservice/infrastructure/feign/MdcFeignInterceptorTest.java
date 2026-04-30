package com.ordertracking.orderservice.infrastructure.feign;

import com.ordertracking.common.mdc.MdcConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class MdcFeignInterceptorTest {

    private MdcFeignInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new MdcFeignInterceptor();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void addsMdcValuesToFeignRequestHeaders() {
        MDC.put(MdcConstants.TRACE_ID,   "trace-abc");
        MDC.put(MdcConstants.REQUEST_ID, "req-xyz");
        MDC.put(MdcConstants.USER_ID,    "user-99");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get(MdcConstants.HEADER_TRACE_ID))
            .containsExactly("trace-abc");
        assertThat(template.headers().get(MdcConstants.HEADER_REQUEST_ID))
            .containsExactly("req-xyz");
        assertThat(template.headers().get(MdcConstants.HEADER_USER_ID))
            .containsExactly("user-99");
    }

    @Test
    void doesNotAddHeadersWhenMdcIsEmpty() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey(MdcConstants.HEADER_TRACE_ID);
        assertThat(template.headers()).doesNotContainKey(MdcConstants.HEADER_REQUEST_ID);
        assertThat(template.headers()).doesNotContainKey(MdcConstants.HEADER_USER_ID);
    }

    @Test
    void addsOnlyPresentMdcValues() {
        MDC.put(MdcConstants.TRACE_ID, "only-trace");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get(MdcConstants.HEADER_TRACE_ID))
            .containsExactly("only-trace");
        assertThat(template.headers()).doesNotContainKey(MdcConstants.HEADER_REQUEST_ID);
        assertThat(template.headers()).doesNotContainKey(MdcConstants.HEADER_USER_ID);
    }
}
