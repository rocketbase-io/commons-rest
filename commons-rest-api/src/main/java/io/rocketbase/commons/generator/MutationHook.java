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
public @interface MutationHook {

    /**
     * name of the method/operation within generated axios-client
     */
    String value() default "";

    /**
     * layout variable ${varInput} @{varResponse} separation by ,<br>
     * <b>important: input variables are named by path/requestParam names not variables (RequestBody is named body!)</b>
     * one invalidation per string (multiple parameters need to be separated by ,
     */
    String[] invalidateKeys() default {};
}
