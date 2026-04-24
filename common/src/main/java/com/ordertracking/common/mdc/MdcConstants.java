package com.ordertracking.common.mdc;

public final class MdcConstants {

    // HTTP Header names
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_TRACE_ID   = "X-Trace-Id";
    public static final String HEADER_USER_ID    = "X-User-Id";

    // MDC key names
    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID   = "traceId";
    public static final String USER_ID    = "userId";
    public static final String METHOD     = "method";
    public static final String URI        = "uri";
    public static final String SERVICE    = "service";
    public static final String CLIENT_IP  = "clientIp";
    public static final String TOPIC      = "topic";
    public static final String PARTITION  = "partition";
    public static final String OFFSET     = "offset";

    private MdcConstants() {}
}
