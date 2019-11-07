package io.rocketbase.commons.logging;

import io.rocketbase.commons.logging.RequestMappingAnnotationUtil.RequestAnnotation;
import lombok.AccessLevel;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.springframework.data.domain.AuditorAware;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRequestLogger extends AbstractLoggingAspect {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");


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
                msg.append(" ⮐ ")
                        .append(toText(config, point));
            }

            addResultWhenEnabled(method, config, result, msg);

            log.debug(msg.toString());
        }
    }


    protected void handleLogging(final StringBuilder msg, ProceedingJoinPoint point, Method method) {
        RequestAnnotation methodRequestMapping = RequestMappingAnnotationUtil.getAnnotation(method, null);
        RequestAnnotation classRequestMapping = RequestMappingAnnotationUtil.getAnnotation(null, point);

        msg.append(getHttpMethod(methodRequestMapping, classRequestMapping))
                .append(" ");

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            msg.append(servletRequestAttributes.getRequest()
                    .getServletPath());
            String queryString = servletRequestAttributes.getRequest()
                    .getQueryString();
            if (config.isQuery() && queryString != null) {
                msg.append("?")
                        .append(queryString);
            }
        } else {
            // useful when used with webflux
            msg.append(getHttpPath(methodRequestMapping, classRequestMapping, point, method));
        }
    }

    protected String getHttpMethod(RequestAnnotation methodRequestMapping, RequestAnnotation classRequestMapping) {
        if (methodRequestMapping != null && methodRequestMapping.getMethod().length == 1) {
            return methodRequestMapping.getMethod()[0].name();
        }
        if (classRequestMapping != null && classRequestMapping.getMethod().length == 1) {
            return classRequestMapping.getMethod()[0].name();
        }
        return "unkown";
    }

    private String getHttpPath(RequestAnnotation methodRequestMapping, RequestAnnotation classRequestMapping, ProceedingJoinPoint point, Method method) {
        StringBuffer sb = new StringBuffer();
        String mappingValue = "unknown";

        if (methodRequestMapping != null && methodRequestMapping.getValue().length == 1) {
            mappingValue = methodRequestMapping.getValue()[0];
        } else if (classRequestMapping != null && classRequestMapping.getValue().length == 1) {
            mappingValue = classRequestMapping.getValue()[0];
        }
        replacePathVariables(method, point, sb, mappingValue);
        if (config.isQuery()) {
            appendQueryString(method, point, sb);
        }

        return sb.toString();
    }


    private void replacePathVariables(Method method, ProceedingJoinPoint point, StringBuffer sb, String mappingValue) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(mappingValue);
        while (matcher
                .find()) {
            String pathVariableName = matcher.group(1);
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
                if (pathVariable != null && pathVariable.value()
                        .equals(pathVariableName)) {
                    Object arg = point.getArgs()[i];
                    matcher.appendReplacement(sb, String.valueOf(arg));
                    break;
                }
            }
        }
        matcher.appendTail(sb);
    }

    private void appendQueryString(Method method, ProceedingJoinPoint point, StringBuffer sb) {
        StringBuilder queryString = new StringBuilder("?");

        boolean first = true;
        Set<String> params = new HashSet<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Object arg = point.getArgs()[i];
                if (MultiValueMap.class.isAssignableFrom(arg.getClass())) {
                    MultiValueMap multiValueMap = (MultiValueMap) arg;
                    for (Object key : multiValueMap.keySet()) {
                        if (!params.contains(String.valueOf(key))) {
                            List values = (List) multiValueMap.get(key);
                            for (Object value : values) {
                                if (!first) {
                                    queryString.append("&");
                                }
                                first = false;
                                queryString.append(key)
                                        .append("=")
                                        .append(value);
                            }
                            params.add(String.valueOf(key));
                        }
                    }
                } else {
                    String key = requestParam.value();
                    if (!params.contains(key)) {
                        if (!first) {
                            queryString.append("&");
                        }
                        first = false;
                        queryString.append(key)
                                .append("=")
                                .append(arg);
                        params.add(key);
                    }
                }
            }

        }
        if (queryString.length() > 1) {
            sb.append(queryString);
        }
    }

}
