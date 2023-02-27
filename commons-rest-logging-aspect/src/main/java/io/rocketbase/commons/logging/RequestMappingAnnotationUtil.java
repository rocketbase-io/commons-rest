package io.rocketbase.commons.logging;

import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class RequestMappingAnnotationUtil {

    private static Class[] annotations = {RequestMapping.class, GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class};

    protected static RequestAnnotation getAnnotation(Method method, ProceedingJoinPoint point, Class clazz) {
        try {
            Annotation annotation = null;
            if (method != null) {
                annotation = method.getAnnotation(clazz);
            } else if (point != null) {
                annotation = point.getTarget().getClass().getAnnotation(clazz);
            }
            RequestAnnotation result = new RequestAnnotation();
            if (clazz.equals(RequestMapping.class)) {
                result.setMethod(((RequestMapping) annotation).method());
                result.setName(((RequestMapping) annotation).name());
                result.setValue(((RequestMapping) annotation).value());
                result.setPath(((RequestMapping) annotation).path());
                result.setParams(((RequestMapping) annotation).params());
                result.setHeaders(((RequestMapping) annotation).headers());
                result.setConsumes(((RequestMapping) annotation).consumes());
                result.setProduces(((RequestMapping) annotation).produces());
            } else if (clazz.equals(GetMapping.class)) {
                result.setMethod(new RequestMethod[]{RequestMethod.GET});
                result.setName(((GetMapping) annotation).name());
                result.setValue(((GetMapping) annotation).value());
                result.setPath(((GetMapping) annotation).path());
                result.setParams(((GetMapping) annotation).params());
                result.setHeaders(((GetMapping) annotation).headers());
                result.setConsumes(((GetMapping) annotation).consumes());
                result.setProduces(((GetMapping) annotation).produces());
            } else if (clazz.equals(PostMapping.class)) {
                result.setMethod(new RequestMethod[]{RequestMethod.POST});
                result.setName(((PostMapping) annotation).name());
                result.setValue(((PostMapping) annotation).value());
                result.setPath(((PostMapping) annotation).path());
                result.setParams(((PostMapping) annotation).params());
                result.setHeaders(((PostMapping) annotation).headers());
                result.setConsumes(((PostMapping) annotation).consumes());
                result.setProduces(((PostMapping) annotation).produces());
            } else if (clazz.equals(PutMapping.class)) {
                result.setMethod(new RequestMethod[]{RequestMethod.PUT});
                result.setName(((PutMapping) annotation).name());
                result.setValue(((PutMapping) annotation).value());
                result.setPath(((PutMapping) annotation).path());
                result.setParams(((PutMapping) annotation).params());
                result.setHeaders(((PutMapping) annotation).headers());
                result.setConsumes(((PutMapping) annotation).consumes());
                result.setProduces(((PutMapping) annotation).produces());
            } else if (clazz.equals(PatchMapping.class)) {
                result.setMethod(new RequestMethod[]{RequestMethod.PATCH});
                result.setName(((PatchMapping) annotation).name());
                result.setValue(((PatchMapping) annotation).value());
                result.setPath(((PatchMapping) annotation).path());
                result.setParams(((PatchMapping) annotation).params());
                result.setHeaders(((PatchMapping) annotation).headers());
                result.setConsumes(((PatchMapping) annotation).consumes());
                result.setProduces(((PatchMapping) annotation).produces());
            } else if (clazz.equals(DeleteMapping.class)) {
                result.setMethod(new RequestMethod[]{RequestMethod.DELETE});
                result.setName(((DeleteMapping) annotation).name());
                result.setValue(((DeleteMapping) annotation).value());
                result.setPath(((DeleteMapping) annotation).path());
                result.setParams(((DeleteMapping) annotation).params());
                result.setHeaders(((DeleteMapping) annotation).headers());
                result.setConsumes(((DeleteMapping) annotation).consumes());
                result.setProduces(((DeleteMapping) annotation).produces());
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static RequestAnnotation getAnnotation(Method method, ProceedingJoinPoint point) {
        if ((method == null && point == null) || (method != null && point != null)) {
            throw new IllegalArgumentException("only method or point is allowed not both together!");
        }
        for (Class c : annotations) {
            RequestAnnotation result = getAnnotation(method, point, c);
            if (result != null) {
                return result;
            }
        }
        return null;
    }


    @Data
    public static class RequestAnnotation {
        private RequestMethod[] method;

        private String name;
        private String[] value;
        private String[] path;
        private String[] params;
        private String[] headers;
        private String[] consumes;
        private String[] produces;
    }
}
