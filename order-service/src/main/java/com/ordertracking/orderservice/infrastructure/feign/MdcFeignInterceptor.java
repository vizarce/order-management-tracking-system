package com.ordertracking.orderservice.infrastructure.feign;

import com.ordertracking.common.mdc.MdcConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        addIfPresent(template, MdcConstants.HEADER_REQUEST_ID, MdcConstants.REQUEST_ID);
        addIfPresent(template, MdcConstants.HEADER_TRACE_ID,   MdcConstants.TRACE_ID);
        addIfPresent(template, MdcConstants.HEADER_USER_ID,    MdcConstants.USER_ID);
    }

    private void addIfPresent(RequestTemplate t, String header, String mdcKey) {
        String val = MDC.get(mdcKey);
        if (val != null) t.header(header, val);
    }
}
