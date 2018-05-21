package io.rocketbase.commons.dto.translation;

import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = HasDefaultLocaleValidator.class)
@Documented
public @interface HasDefaultLocale {
    String message() default "{constraints.hasDefaultLocale}";

    Class<?>[] groups() default {};

}
