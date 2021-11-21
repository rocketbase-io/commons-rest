package io.rocketbase.commons.resource;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

/**
 * custom annotation that should be added to all rest-controller methods to specify js-client-operation-name
 */
@Target({METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ClientOperation {

    /**
     * name of the method/operation within generated axios-client
     *
     * @return
     */
    String value();

    HookType type() default HookType.INFINITE;


    /**
     * layout pathVariable  ${variableName} separation by ,
     *
     * @return
     */
    String cacheKeys() default "";

    /**
     * layout variable ${varInput} @{varResponse} separation by ,<br>
     * one invalidation per string (multiple parameters need to be separated by ,
     *
     * @return
     */
    String[] invalidateKeys() default {};

    /**
     * stale time for key-access in seconds
     *
     * @return
     */
    int staleTime() default 2;

}
