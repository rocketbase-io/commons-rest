package io.rocketbase.commons.generator;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

@Target({METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InfiniteHook {

    /**
     * name of the method/operation within generated axios-client<br>
     * when not set use method name
     */
    String value() default "";

    /**
     * layout pathVariable  ${variableName} separation by ,
     */
    String cacheKeys();

    /**
     * stale time for key-access in seconds
     */
    int staleTime() default 2;
}
