package com.ordertracking.common.mdc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MdcConstantsTest {

    @Test
    void shouldDefineHttpHeaderConstants() {
        assertThat(MdcConstants.HEADER_REQUEST_ID).isEqualTo("X-Request-Id");
        assertThat(MdcConstants.HEADER_TRACE_ID).isEqualTo("X-Trace-Id");
        assertThat(MdcConstants.HEADER_USER_ID).isEqualTo("X-User-Id");
    }

    @Test
    void shouldDefineMdcKeyConstants() {
        assertThat(MdcConstants.REQUEST_ID).isEqualTo("requestId");
        assertThat(MdcConstants.TRACE_ID).isEqualTo("traceId");
        assertThat(MdcConstants.USER_ID).isEqualTo("userId");
        assertThat(MdcConstants.METHOD).isEqualTo("method");
        assertThat(MdcConstants.URI).isEqualTo("uri");
        assertThat(MdcConstants.SERVICE).isEqualTo("service");
        assertThat(MdcConstants.CLIENT_IP).isEqualTo("clientIp");
        assertThat(MdcConstants.TOPIC).isEqualTo("topic");
        assertThat(MdcConstants.PARTITION).isEqualTo("partition");
        assertThat(MdcConstants.OFFSET).isEqualTo("offset");
    }
}
