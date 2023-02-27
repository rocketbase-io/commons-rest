package io.rocketbase.commons.posthog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * allow to add property to capture
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostHogProperty {


    /**
     * name of property that should be set to properties value als key
     */
    String name();

}
