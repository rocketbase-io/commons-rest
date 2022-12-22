package io.rocketbase.commons.logging;

import io.rocketbase.commons.util.TimeUtil;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.beans.Transient;

@Data
public class RequestLoggingInfo {

    private String method;

    private String path;

    @Nullable
    private String query;

    @Nullable
    private Object auditor;

    @Nullable
    private Long duration;

    @Nullable
    private String args;

    @Nullable
    private String result;

    @Nullable
    private String errorMessage;

    @Nullable
    private String stacktrace;

    protected StringBuffer initMessage() {
        StringBuffer str = new StringBuffer();
        str.append("[").append(method).append("]").append(" ").append(path);
        return str;
    }

    @Transient
    public String toLogMessage() {
        StringBuffer str = initMessage();
        if (query != null) {
            str.append(query);
        }
        if (auditor != null) {
            str.append(AbstractLoggingAspect.USER_SIGN).append(auditor);
        }
        if (duration != null) {
            str.append(AbstractLoggingAspect.TIME_SIGN).append(TimeUtil.convertMillisToFormatted(duration));
        }
        if (args != null) {
            str.append(AbstractLoggingAspect.ARGS_SIGN).append(args);
        }
        if (result != null) {
            str.append(AbstractLoggingAspect.RETURN_SIGN).append(result);
        }
        return str.toString();
    }

    @Transient
    public String toErrorMessage() {
        StringBuffer str = initMessage();
        if (query != null) {
            str.append(query);
        }
        if (auditor != null) {
            str.append(AbstractLoggingAspect.USER_SIGN).append(auditor);
        }
        if (errorMessage != null) {
            str.append(" throw ").append(errorMessage);
            if (stacktrace != null) {
                str.append(" ").append(stacktrace);
            }
        }

        if (duration != null) {
            str.append(AbstractLoggingAspect.TIME_SIGN).append(TimeUtil.convertMillisToFormatted(duration));
        }
        if (args != null) {
            str.append(AbstractLoggingAspect.ARGS_SIGN).append(args);
        }
        return str.toString();
    }
}
