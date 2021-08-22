package io.rocketbase.commons.logging;

public interface RequestLoggingInterceptor {

    void afterSuccess(RequestLoggingInfo info);

    void afterFailure(RequestLoggingInfo info, Throwable throwable);
}
