package io.rocketbase.commons.logging;

import lombok.AccessLevel;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

public abstract class AbstractRequestLogger extends AbstractLoggingAspect {

    @Getter(AccessLevel.PROTECTED)
    private final LoggableConfig config;

    public AbstractRequestLogger(AuditorAware auditorAware, LoggableConfig config) {
        super(auditorAware);
        this.config = config;
    }

    protected void afterSuccess(Logger log, ProceedingJoinPoint point, Method method, long start, Object result) {
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
    }


    protected void handleLogging(final StringBuilder msg, ProceedingJoinPoint point, Method method) {
        RequestMappingAnnotationUtil.RequestAnnotation methodRequestMapping = RequestMappingAnnotationUtil.getAnnotation(method, null);
        RequestMappingAnnotationUtil.RequestAnnotation classRequestMapping = RequestMappingAnnotationUtil.getAnnotation(null, point);

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

    protected String getHttpMethod(RequestMappingAnnotationUtil.RequestAnnotation methodRequestMapping, RequestMappingAnnotationUtil.RequestAnnotation classRequestMapping) {
        if (methodRequestMapping != null && methodRequestMapping.getMethod().length == 1) {
            return methodRequestMapping.getMethod()[0].name();
        }
        if (classRequestMapping != null && classRequestMapping.getMethod().length == 1) {
            return classRequestMapping.getMethod()[0].name();
        }
        return "unkown";
    }

    protected String getHttpPath(RequestMappingAnnotationUtil.RequestAnnotation methodRequestMapping, RequestMappingAnnotationUtil.RequestAnnotation classRequestMapping) {
        if (methodRequestMapping != null && methodRequestMapping.getValue().length == 1) {
            return methodRequestMapping.getValue()[0];
        }
        if (classRequestMapping != null && classRequestMapping.getValue().length == 1) {
            return classRequestMapping.getValue()[0];
        }
        return "unkown";
    }
}
