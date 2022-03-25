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
     *
     * @return
     */
    String value();

    /**
     * layout variable ${varInput} @{varResponse} separation by ,<br>
     * one invalidation per string (multiple parameters need to be separated by ,
     *
     * @return
     */
    String[] invalidateKeys() default {};
}
