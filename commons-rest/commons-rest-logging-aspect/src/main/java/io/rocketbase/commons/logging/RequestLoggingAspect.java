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

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    public RequestLoggingAspect(AuditorAware auditorAware, LoggableConfig config, RequestLoggingInterceptor requestLoggingInterceptor) {
        super(auditorAware, config);
        this.requestLoggingInterceptor = requestLoggingInterceptor;
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
            RequestLoggingInfo info = extractBaseInfo(point, method, start);
            afterSuccess(log, info, method, result);

            if (requestLoggingInterceptor != null) {
                requestLoggingInterceptor.afterSuccess(info);
            }

            return result;
        } catch (Throwable ex) {
            RequestLoggingInfo info = extractBaseInfo(point, method, start);
            afterFailure(log, info, point, ex);

            if (requestLoggingInterceptor != null) {
                requestLoggingInterceptor.afterFailure(info, ex);
            }

            throw ex;
        }
    }

    private RequestLoggingInfo extractBaseInfo(ProceedingJoinPoint point, Method method, long start) {
        RequestLoggingInfo info = extractBase(point, method);
        handleCurrentAuditor(null, info);

        if (getConfig().isDuration()) {
            info.setDuration(System.currentTimeMillis() - start);
        }

        if (getConfig().isArgs()) {
            info.setArgs(toText(getConfig(), point));
        }
        return info;
    }
}
