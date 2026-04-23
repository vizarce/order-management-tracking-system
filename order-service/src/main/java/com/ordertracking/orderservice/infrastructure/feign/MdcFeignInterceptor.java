package com.ordertracking.orderservice.infrastructure.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        addIfPresent(template, "X-Request-Id", "requestId");
        addIfPresent(template, "X-Trace-Id", "traceId");
        addIfPresent(template, "X-User-Id", "userId");
    }

    private void addIfPresent(RequestTemplate t, String header, String mdcKey) {
        String val = MDC.get(mdcKey);
        if (val != null) t.header(header, val);
    }
}
