package io.rocketbase.commons.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleResolverConfig {

    @Value("${locale.default:en}")
    private String defaultLocale;

    @Bean
    public LocaleResolver localeResolver() {
        FixedLocaleResolver l = new FixedLocaleResolver();
        l.setDefaultLocale(Locale.forLanguageTag(defaultLocale));
        return l;
    }
}
