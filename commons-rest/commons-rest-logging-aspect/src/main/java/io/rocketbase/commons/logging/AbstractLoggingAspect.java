package io.rocketbase.commons.logging;

import io.rocketbase.commons.util.TimeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.data.domain.AuditorAware;

import java.lang.reflect.Method;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractLoggingAspect extends LogHelper {

    public static final String USER_SIGN = " ツ ";
    public static final String TIME_SIGN = " \uD83D\uDD53 ";
    public static final String ARGS_SIGN = " ⮐ ";
    public static final String RETURN_SIGN = " ⮑ ";

    @Getter(AccessLevel.PROTECTED)
    private final AuditorAware auditorAware;

    protected Logger getLog(ProceedingJoinPoint point) {
        return getLog(point.getTarget()
                .getClass());
    }

    protected Logger getLog(Class clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    protected void printLog(Logger logger, Level level, String message) {
        switch (level) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }

    protected boolean isLogEnabled(Logger logger, Level level) {
        boolean result = false;
        switch (level) {
            case TRACE:
                result = logger.isTraceEnabled();
                break;
            case DEBUG:
                result = logger.isDebugEnabled();
                break;
            case INFO:
                result = logger.isInfoEnabled();
                break;
            case WARN:
                result = logger.isWarnEnabled();
                break;
            case ERROR:
                result = logger.isErrorEnabled();
                break;
        }
        return result;
    }

    protected void addUserWhenEnabled(LoggableConfig config, StringBuilder msg) {
        if (config.isAudit()) {
            addUserWhenPossible(lookupCurrentAuditor(), msg);
        }
    }

    protected void addUserWhenPossible(Optional optional, StringBuilder msg) {
        if (optional.isPresent()) {
            msg.append(USER_SIGN).append(optional.get());
        }
    }

    /**
     * allows to overwrite way of user lookup
     */
    protected Optional lookupCurrentAuditor() {
        return auditorAware.getCurrentAuditor();
    }

    protected void addDurationWhenEnabled(LoggableConfig config, long start, StringBuilder append) {
        if (config.isDuration()) {
            append.append(TIME_SIGN)
                    .append(TimeUtil.convertMillisToMinSecFormat(System.currentTimeMillis() - start));
        }
    }

    protected void addResultWhenEnabled(Method method, LoggableConfig config, Object result, StringBuilder msg) {
        if (!Void.TYPE.equals(method.getReturnType()) && config.isResult()) {
            msg.append(RETURN_SIGN).append(objToText(config, result));
        }
    }

    protected void logError(ProceedingJoinPoint point, LoggableConfig config, long start, Logger log, Throwable ex) {
        if (isLogEnabled(log, config.getErrorLogLevel())) {
            StringBuilder msg = new StringBuilder();
            msg.append(toText(config, point));
            addUserWhenEnabled(config, msg);

            msg.append(": thrown ");
            msg.append(throwableToText(ex));

            msg.append(" ").append(getStacktraceInfo(ex));

            addDurationWhenEnabled(config, start, msg);
            printLog(log, config.getErrorLogLevel(), msg.toString());
        }
    }

    protected String getStacktraceInfo(Throwable ex) {
        StackTraceElement trace = ex.getStackTrace()[0];
        return String.format("@%s#%s line: %s", trace.getClassName(), trace.getMethodName(), trace.getLineNumber());
    }

}
