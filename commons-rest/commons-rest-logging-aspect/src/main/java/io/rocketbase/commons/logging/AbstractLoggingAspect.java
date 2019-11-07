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

    protected void addUserWhenPossible(StringBuilder msg) {
        Optional optional = auditorAware.getCurrentAuditor();
        if (optional.isPresent()) {
            msg.append(" ツ ").append(optional.get());
        }
    }

    protected void addDurationWhenEnabled(LoggableConfig config, long start, StringBuilder append) {
        if (config.isDuration()) {
            append.append(" \uD83D\uDD53 ")
                    .append(TimeUtil.convertMillisToMinSecFormat(System.currentTimeMillis() - start));
        }
    }

    protected void addResultWhenEnabled(Method method, LoggableConfig config, Object result, StringBuilder msg) {
        if (!Void.TYPE.equals(method.getReturnType()) && !config.isSkipResult()) {
            msg.append(" ⮑ ").append(objToText(config, result));
        }
    }

    protected void logError(ProceedingJoinPoint point, LoggableConfig config, long start, Logger log, Throwable ex) {
        if (config.getErrorLogLevel() != null) {
            if (isLogEnabled(log, config.getErrorLogLevel())) {
                StringBuilder msg = new StringBuilder();
                msg.append(toText(config, point));
                addUserWhenPossible(msg);

                msg.append(": thrown ");
                msg.append(throwableToText(ex));

                StackTraceElement trace = ex.getStackTrace()[0];
                msg.append(" @")
                        .append(trace.getClassName())
                        .append('#')
                        .append(trace.getMethodName())
                        .append(" line: ")
                        .append(trace.getLineNumber());


                addDurationWhenEnabled(config, start, msg);
                printLog(log, config.getErrorLogLevel(), msg.toString());
            }
        }
    }

}
