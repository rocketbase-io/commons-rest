package io.rocketbase.commons.logging;

public interface RequestLogginInterceptor {

    void afterSuccess(RequestLoggingInfo info);

    void afterFailure(RequestLoggingInfo info, Throwable throwable);
}
