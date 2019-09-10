package io.rocketbase.commons.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.data.domain.AuditorAware;

import java.lang.reflect.Method;

@Aspect
public class MethodLogger extends AbstractLoggingAspect {

    public MethodLogger(AuditorAware auditorAware) {
        super(auditorAware);
    }

    @Around("execution(* *(..)) && @annotation(io.rocketbase.commons.logging.Loggable)")
    public Object wrapMethod(ProceedingJoinPoint point) throws Throwable {
        Method method = MethodSignature.class.cast(point.getSignature()).getMethod();

        Class<?> targetClass = point.getTarget().getClass();
        Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());

        Loggable loggable = targetMethod.getAnnotation(Loggable.class);
        return this.wrap(point, method, new LoggableConfig(loggable));
    }

    private Object wrap(ProceedingJoinPoint point, Method method, LoggableConfig config) throws Throwable {
        long start = System.currentTimeMillis();

        Logger log = getLog(point);
        try {
            Object result = point.proceed();
            if (isLogEnabled(log, config.getLogLevel())) {
                StringBuilder msg = new StringBuilder();

                msg.append(toText(config, point));
                addUserWhenPossible(msg);
                addDurationWhenEnabled(config, start, msg);
                addResultWhenEnabled(method, config, result, msg);

                printLog(log, config.getLogLevel(), msg.toString());
            }
            return result;
        } catch (Throwable ex) {
            logError(point, config, start, log, ex);
            throw ex;
        }
    }

}
