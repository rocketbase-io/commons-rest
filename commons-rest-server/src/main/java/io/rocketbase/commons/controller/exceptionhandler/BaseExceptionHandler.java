package io.rocketbase.commons.controller.exceptionhandler;

import io.rocketbase.commons.dto.ErrorResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /**
     * wraps the given body as RFC 9457 problem response with application/problem+json content-type<br>
     * fills status + title defaults when missing
     */
    protected ResponseEntity<ErrorResponse> problem(HttpStatus status, @Nullable ErrorResponse body) {
        ErrorResponse result = body != null ? body : new ErrorResponse(status.value(), null);
        if (result.getStatus() == null) {
            result.setStatus(status.value());
        }
        if (result.getTitle() == null) {
            result.setTitle(status.getReasonPhrase());
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(result);
    }
}
