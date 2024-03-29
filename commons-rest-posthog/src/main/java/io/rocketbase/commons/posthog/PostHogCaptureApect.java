package io.rocketbase.commons.posthog;

import io.rocketbase.commons.config.PostHogProperties;
import io.rocketbase.commons.util.Nulls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class PostHogCaptureApect {

    public final PostHogService postHogService;
    public final PostHogProperties config;

    @Around("execution(* *(..)) && @annotation(io.rocketbase.commons.posthog.PostHogCapture)")
    public Object wrapMethod(ProceedingJoinPoint point) throws Throwable {
        Method method = MethodSignature.class.cast(point.getSignature()).getMethod();

        Class<?> targetClass = point.getTarget().getClass();

        Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
        PostHogCapture capture = targetMethod.getAnnotation(PostHogCapture.class);
        Map<String, Object> properties = new HashMap<>();

        try {
            Object[] args = point.getArgs();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int argIndex = 0; argIndex < args.length; argIndex++) {
                for (Annotation annotation : parameterAnnotations[argIndex]) {
                    if (!(annotation instanceof PostHogProperty))
                        continue;
                    PostHogProperty postHogProperty = (PostHogProperty) annotation;
                    properties.put(postHogProperty.name(), args[argIndex]);
                }
            }
        } catch (Exception e) {
            log.warn("parsing properties for method {} got errors: {}", method.getName(), e.getMessage());
        }

        long start = System.currentTimeMillis();
        Object result = point.proceed();
        if (config.isCaptureWithDuration()) {
            properties.put("duration-ms", System.currentTimeMillis() - start);
        }
        try {
            postHogService.current().capture(Nulls.notEmpty(capture.name(), method.getName()));
        } catch (Exception e) {
            log.warn("couldn't capture {}, {}", method.getName(), e.getMessage());
        }
        return result;
    }

}
