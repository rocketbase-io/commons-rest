package io.rocketbase.commons.openapi;

import io.rocketbase.commons.resource.ClientOperation;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
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
        extensions.put(CONTROLLER_BEAN, handlerMethod.getBeanType().getName());
        extensions.put(METHOD_NAME, handlerMethod.getMethod().getName());

        if (handlerMethod.hasMethodAnnotation(ClientOperation.class)) {
            ClientOperation annotation = handlerMethod.getMethodAnnotation(ClientOperation.class);
            extensions.put(METHOD_NAME, annotation.value());
            extensions.put(HOOK_TYPE, annotation.type());
            extensions.put(CACHE_KEYS, annotation.cacheKeys());
            extensions.put(INVALIDATE_KEYS, annotation.invalidateKeys());
            extensions.put(STALE_TIME, annotation.staleTime());
        }

        if (!extensions.isEmpty()) {
            if (operation.getExtensions() != null) {
                operation.getExtensions().put("extensions", extensions);
            } else {
                Map<String, Object> map = new HashMap();
                map.put("extensions", extensions);
                operation.setExtensions(map);
            }
        }
        return operation;
    }
}
