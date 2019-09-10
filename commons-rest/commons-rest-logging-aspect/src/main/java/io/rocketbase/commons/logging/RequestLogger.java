package io.rocketbase.commons.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
public class RequestLogger extends AbstractLoggingAspect {

    private final LoggableConfig config;

    public RequestLogger(AuditorAware auditorAware, LoggableConfig config) {
        super(auditorAware);
        this.config = config;
    }

    @Around("execution(* *(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object wrapMethod(ProceedingJoinPoint point) throws Throwable {
        Method method = MethodSignature.class.cast(point.getSignature()).getMethod();

        return this.wrap(point, method);
    }

    private Object wrap(ProceedingJoinPoint point, Method method) throws Throwable {
        long start = System.currentTimeMillis();

        Logger log = getLog(point);
        try {
            Object result = point.proceed();
            if (log.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();

                handleLogging(msg, point, method);
                addUserWhenPossible(msg);
                addDurationWhenEnabled(config, start, msg);


                if (!config.isSkipArgs()) {
                    msg.append(" ⮐ ").append(toText(config, point));
                }

                addResultWhenEnabled(method, config, result, msg);

                log.debug(msg.toString());
            }
            return result;
        } catch (Throwable ex) {
            logError(point, config, start, log, ex);
            throw ex;
        }
    }

    public void handleLogging(final StringBuilder msg, ProceedingJoinPoint point, Method method) {
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        RequestMapping classRequestMapping = point.getTarget().getClass().getAnnotation(RequestMapping.class);

        msg.append(getHttpMethod(methodRequestMapping, classRequestMapping)).append(" ");

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            msg.append(servletRequestAttributes.getRequest().getServletPath());
            String queryString = servletRequestAttributes.getRequest().getQueryString();
            if (queryString != null) {
                msg.append("?").append(queryString);
            }
        } else {
            msg.append(getHttpPath(methodRequestMapping, classRequestMapping));
        }
    }

    private String getHttpMethod(RequestMapping methodRequestMapping, RequestMapping classRequestMapping) {
        if (methodRequestMapping != null && methodRequestMapping.method().length == 1) {
            return methodRequestMapping.method()[0].name();
        }
        if (classRequestMapping != null && classRequestMapping.method().length == 1) {
            return classRequestMapping.method()[0].name();
        }
        return "unkown";
    }

    private String getHttpPath(RequestMapping methodRequestMapping, RequestMapping classRequestMapping) {
        if (methodRequestMapping != null && methodRequestMapping.value().length == 1) {
            return methodRequestMapping.value()[0];
        }
        if (classRequestMapping != null && classRequestMapping.value().length == 1) {
            return classRequestMapping.value()[0];
        }
        return "unkown";
    }
}
