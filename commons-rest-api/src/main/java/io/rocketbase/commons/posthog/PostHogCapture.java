package io.rocketbase.commons.posthog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotated methods will trigger a capture with user identity information to posthog<br>
 * this annotation is only working on spring proxied beans!<br>
 * internal method call within the bean itself will not get tracked - other bean calls will work
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostHogCapture {
    
    /**
     * event name<br>
     * when not set use method name
     */
    String name() default "";
}
