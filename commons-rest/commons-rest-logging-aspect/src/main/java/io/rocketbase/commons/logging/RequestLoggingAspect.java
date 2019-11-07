package io.rocketbase.commons.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.data.domain.AuditorAware;

import java.lang.reflect.Method;

@Aspect
public class RequestLoggingAspect extends AbstractRequestLogger {

    public RequestLoggingAspect(AuditorAware auditorAware, LoggableConfig config) {
        super(auditorAware, config);
    }

    @Around("execution(* *(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "))")
    public Object wrapMethod(ProceedingJoinPoint point) throws Throwable {
        Method method = MethodSignature.class.cast(point.getSignature()).getMethod();

        long start = System.currentTimeMillis();

        Logger log = getLog(point);
        try {
            Object result = point.proceed();
            afterSuccess(log, point, method, start, result);
            return result;
        } catch (Throwable ex) {
            logError(point, getConfig(), start, log, ex);
            throw ex;
        }
    }
}
