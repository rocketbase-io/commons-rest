package io.rocketbase.commons.controller.exceptionhandler;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class BaseExceptionHandler {

    @Resource
    private LocaleResolver localeResolver;

    @Resource
    private MessageSource messageSource;

    protected String translate(HttpServletRequest request, String messageProperty, String defaultMessage, Object... args) {
        Locale locale = localeResolver.resolveLocale(request);
        return messageSource.getMessage(messageProperty, args, defaultMessage, locale);
    }
}
