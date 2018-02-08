package io.rocketbase.commons.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleResolverConfig {

    @Value("${locale.resolver.default:en}")
    private String defaultLocale;

    @Bean
    @ConditionalOnProperty(name = "locale.resolver.enabled", matchIfMissing = true)
    public LocaleResolver localeResolver() {
        FixedLocaleResolver l = new FixedLocaleResolver();
        l.setDefaultLocale(Locale.forLanguageTag(defaultLocale));
        return l;
    }
}
