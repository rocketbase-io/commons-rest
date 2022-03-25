package io.rocketbase.commons.openapi;

import io.rocketbase.commons.generator.ClientModule;
import io.rocketbase.commons.generator.InfiniteHook;
import io.rocketbase.commons.generator.MutationHook;
import io.rocketbase.commons.generator.QueryHook;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * extends open-api to add annotation information to each handels method
 */
public class OpenApiCustomExtractor implements OperationCustomizer {

    public static final String GENERIC_RETURN_TYPE = "genericReturnType";
    public static final String PARAMETER_TYPES = "parameterTypes";
    public static final String CONTROLLER_BEAN = "controllerBean";
    public static final String METHOD_NAME = "methodName";
    public static final String HOOK_TYPE = "hookType";
    public static final String CACHE_KEYS = "cacheKeys";
    public static final String INVALIDATE_KEYS = "invalidateKeys";
    public static final String STALE_TIME = "staleTime";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Map<String, Object> extensions = new HashMap<>();

        if (handlerMethod.getMethod().getGenericReturnType() instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) handlerMethod.getMethod().getGenericReturnType();

            if (type.getRawType().equals(ResponseEntity.class)) {
                extensions.put(GENERIC_RETURN_TYPE, type.getActualTypeArguments()[0].getTypeName());
            } else {
                extensions.put(GENERIC_RETURN_TYPE, type.getTypeName());
            }
            extensions.put(PARAMETER_TYPES, Arrays.stream(handlerMethod.getMethod().getGenericParameterTypes()).map(v -> v.getTypeName()).collect(Collectors.toList()));
        }

        extensions.put(CONTROLLER_BEAN, extractControllerBean(handlerMethod));

        if (handlerMethod.hasMethodAnnotation(InfiniteHook.class)) {
            InfiniteHook annotation = handlerMethod.getMethodAnnotation(InfiniteHook.class);
            extensions.put(METHOD_NAME, extractMethodName(annotation.value(), handlerMethod));
            extensions.put(HOOK_TYPE, "infinite");
            extensions.put(CACHE_KEYS, annotation.cacheKeys());
            extensions.put(STALE_TIME, annotation.staleTime());
        } else if (handlerMethod.hasMethodAnnotation(QueryHook.class)) {
            QueryHook annotation = handlerMethod.getMethodAnnotation(QueryHook.class);
            extensions.put(METHOD_NAME, extractMethodName(annotation.value(), handlerMethod));
            extensions.put(HOOK_TYPE, "query");
            extensions.put(CACHE_KEYS, annotation.cacheKeys());
            extensions.put(STALE_TIME, annotation.staleTime());
        } else if (handlerMethod.hasMethodAnnotation(MutationHook.class)) {
            MutationHook annotation = handlerMethod.getMethodAnnotation(MutationHook.class);
            extensions.put(METHOD_NAME, extractMethodName(annotation.value(), handlerMethod));
            extensions.put(HOOK_TYPE, "mutation");
            extensions.put(INVALIDATE_KEYS, annotation.invalidateKeys());
        }

        if (operation.getExtensions() == null) {
            operation.setExtensions(new HashMap());
        }
        operation.getExtensions().put("extensions", extensions);

        return operation;
    }

    private String extractControllerBean(HandlerMethod handlerMethod) {
        String controllerBean = handlerMethod.getBeanType().getName();
        if (handlerMethod.getBeanType().isAnnotationPresent(ClientModule.class)) {
            String clientModuleCustomName = handlerMethod.getBeanType().getAnnotation(ClientModule.class).value();
            if (StringUtils.hasText(clientModuleCustomName)) {
                controllerBean = clientModuleCustomName;
            }
        }
        return controllerBean;
    }

    protected String extractMethodName(String configuredValue, HandlerMethod handlerMethod) {
        if (StringUtils.hasText(configuredValue)) {
            return configuredValue;
        } else {
            return handlerMethod.getMethod().getName();
        }
    }
}
