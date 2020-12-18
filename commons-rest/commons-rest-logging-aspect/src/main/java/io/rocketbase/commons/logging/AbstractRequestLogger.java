package io.rocketbase.commons.logging;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.exception.InsufficientPrivilegesException;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
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
import java.util.Optional;
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

    /**
     * normal version used within a servlet context
     */
    protected void afterSuccess(Logger log, RequestLoggingInfo info, Method method, Object result) {
        afterSuccess(log, info, method, result, null);
    }

    /**
     * allows to inject current auditor information<br>
     * in flux context the auditAware information is gone (no security context anymore)
     */
    protected void afterSuccess(Logger log, RequestLoggingInfo info, Method method, Object result, Optional currentAuditor) {
        if (isLogEnabled(log, config.getLogLevel())) {

            if (!Void.TYPE.equals(method.getReturnType()) && config.isResult()) {
                info.setResult(objToText(config, result == null ? "null" : result));
            }

            printLog(log, config.getLogLevel(), info.toLogMessage());
        }
    }

    protected void afterFailure(Logger log, RequestLoggingInfo info, ProceedingJoinPoint point, Throwable ex) {
        if (isLogEnabled(log, config.getErrorLogLevel())) {

            if (ex instanceof NotFoundException) {
                info.setErrorMessage("NotFoundException");
            } else if (ex instanceof InsufficientPrivilegesException) {
                info.setErrorMessage("InsufficientPrivilegesException");
            } else if (ex instanceof ObfuscatedDecodeException) {
                info.setErrorMessage("ObfuscatedDecodeException");
            } else if (ex instanceof BadRequestException) {
                info.setErrorMessage("BadRequestException");
                ErrorResponse errorResponse = ((BadRequestException) ex).getErrorResponse();
                info.setStacktrace(errorResponse != null ? errorResponse.toString() : null);
            } else {
                info.setErrorMessage(throwableToText(ex));
                info.setStacktrace(getStacktraceInfo(ex));
            }

            printLog(log, config.getErrorLogLevel(), info.toErrorMessage());
        }
    }

    protected void handleCurrentAuditor(Optional currentAuditor, RequestLoggingInfo info) {
        if (currentAuditor != null) {
            info.setAuditor(currentAuditor.orElse(null));
        } else {
            info.setAuditor(lookupCurrentAuditor().orElse(null));
        }
    }

    protected RequestLoggingInfo extractBase(ProceedingJoinPoint point, Method method) {
        RequestAnnotation methodRequestMapping = RequestMappingAnnotationUtil.getAnnotation(method, null);
        RequestAnnotation classRequestMapping = RequestMappingAnnotationUtil.getAnnotation(null, point);

        RequestLoggingInfo info = new RequestLoggingInfo();
        info.setMethod(getHttpMethod(methodRequestMapping, classRequestMapping));

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            handleRequestInfo((ServletRequestAttributes) requestAttributes, info);
        } else {
            // useful when used with webflux
            handleFluxRequestInfo(methodRequestMapping, classRequestMapping, point, method, info);
        }
        return info;
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

    private void handleRequestInfo(ServletRequestAttributes servletRequestAttributes, RequestLoggingInfo info) {
        info.setPath(servletRequestAttributes.getRequest().getServletPath());
        info.setQuery(servletRequestAttributes.getRequest().getQueryString());
    }

    private void handleFluxRequestInfo(RequestAnnotation methodRequestMapping, RequestAnnotation classRequestMapping, ProceedingJoinPoint point, Method method, RequestLoggingInfo info) {
        StringBuffer sb = new StringBuffer();
        String mappingValue = "unknown";

        if (methodRequestMapping != null && methodRequestMapping.getValue().length == 1) {
            mappingValue = methodRequestMapping.getValue()[0];
        } else if (classRequestMapping != null && classRequestMapping.getValue().length == 1) {
            mappingValue = classRequestMapping.getValue()[0];
        }
        replacePathVariables(method, point, sb, mappingValue);
        info.setPath(sb.toString());
        info.setQuery(getFluxQueryString(method, point, sb));
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

    private String getFluxQueryString(Method method, ProceedingJoinPoint point, StringBuffer sb) {
        StringBuilder queryString = new StringBuilder("");

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
        if (queryString.length() > 0) {
            return queryString.toString();
        } else {
            return null;
        }
    }

}
