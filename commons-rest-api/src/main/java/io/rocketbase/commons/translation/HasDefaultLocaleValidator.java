package io.rocketbase.commons.translation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

public class HasDefaultLocaleValidator implements ConstraintValidator<HasDefaultLocale, Map<Locale, String>> {
    @Override
    public void initialize(HasDefaultLocale constraintAnnotation) {

    }

    @Override
    public boolean isValid(Map<Locale, String> value, ConstraintValidatorContext context) {
        if (value != null) {
            Set<Locale> locales = new HashSet<>(Arrays.asList(LocaleContextHolder.getLocale(), Locale.ENGLISH));

            for (Locale locale : locales) {
                if (value.containsKey(locale)) {
                    if (value.get(locale) != null && !value.get(locale)
                            .isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
