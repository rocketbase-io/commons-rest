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
public @interface QueryHook {

    /**
     * name of the method/operation within generated axios-client
     */
    String value() default "";

    /**
     * layout pathVariable  ${variableName} separation by ,
     *
     * @return
     */
    String cacheKeys() default "";

    /**
     * stale time for key-access in seconds<br>
     * 0 or less means disabled staleTime<br>
     * default value could get configured via properties (-1 is use default)
     */
    int staleTime() default -1;
}
