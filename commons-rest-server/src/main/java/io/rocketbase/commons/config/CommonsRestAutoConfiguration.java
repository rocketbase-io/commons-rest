package io.rocketbase.commons.config;

import io.rocketbase.commons.controller.exceptionhandler.BadRequestExceptionHandler;
import io.rocketbase.commons.controller.exceptionhandler.BeanValidationExceptionHandler;
import io.rocketbase.commons.controller.exceptionhandler.InsufficientPrivilegesExceptionHandler;
import io.rocketbase.commons.controller.exceptionhandler.NotFoundExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class CommonsRestAutoConfiguration {

    @Value("${locale.resolver.default:en}")
    private String defaultLocale;

    @Value("${locale.resolver.supported:}")
    private List<Locale> supportedLocales;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "locale.resolver.enabled", matchIfMissing = true)
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver l = new AcceptHeaderLocaleResolver();
        l.setDefaultLocale(Locale.forLanguageTag(defaultLocale));
        if (supportedLocales != null && !supportedLocales.isEmpty()) {
            l.setSupportedLocales(supportedLocales);
        }
        return l;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "handler.badRequest.enabled", matchIfMissing = true)
    public BadRequestExceptionHandler badRequestExceptionHandler() {
        return new BadRequestExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "handler.notFound.enabled", matchIfMissing = true)
    public NotFoundExceptionHandler notFoundExceptionHandler() {
        return new NotFoundExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "handler.beanValidation.enabled", matchIfMissing = true)
    public BeanValidationExceptionHandler beanValidationExceptionHandler() {
        return new BeanValidationExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "handler.insufficientPrivileges.enabled", matchIfMissing = true)
    public InsufficientPrivilegesExceptionHandler insufficientPrivilegesExceptionHandler() {
        return new InsufficientPrivilegesExceptionHandler();
    }

}
