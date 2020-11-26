package io.rocketbase.commons.translation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface Translated {

    /**
     * specify locale that should be used<br>
     * when value is empty - {@link org.springframework.context.i18n.LocaleContextHolder} is been used
     * otherwise value is been parsed by Locale.forLanguageTag()
     */
    String value() default "";
}
