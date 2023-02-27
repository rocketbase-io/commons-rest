package io.rocketbase.commons.config;

import io.rocketbase.commons.controller.exceptionhandler.ObfuscatedDecodeExceptionHandler;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import io.rocketbase.commons.obfuscated.IdObfuscator;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import io.rocketbase.commons.obfuscated.ObfuscatedIdDeserializer;
import io.rocketbase.commons.obfuscated.SimpleObfuscatedId;
import io.rocketbase.commons.service.DefaultIdObfuscator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.beans.PropertyEditorSupport;
import java.util.Locale;

@Configuration
public class CommonsRestHashIdsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdObfuscator idObfuscator(@Value("${hashids.salt:}") String salt, @Value("${hashids.minHashLength:8}") int minHashLength, @Value("${hashids.alphabet:abcdefghijklmnopqrstuvwxyz1234567890}") String alphabet) {
        return new DefaultIdObfuscator(new Hashids(salt, minHashLength, alphabet));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "hashids.handler.enabled", matchIfMissing = true)
    public ObfuscatedDecodeExceptionHandler obfuscatedDecodeExceptionHandler() {
        return new ObfuscatedDecodeExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObfuscatedIdDeserializer obfuscatedIdDeserializer(@Autowired IdObfuscator idObfuscator, @Value("${hashids.invalid.allowed:false}") boolean invalidAllowed) {
        return new ObfuscatedIdDeserializer(idObfuscator, invalidAllowed);
    }

    @RestControllerAdvice
    public static class Advice {

        @Autowired
        IdObfuscator obfuscator;

        @Value("${hashids.invalid.allowed:false}")
        boolean invalidAllowed;

        @InitBinder
        public void addSupportForObfuscatedId(WebDataBinder binder) {
            binder.registerCustomEditor(ObfuscatedId.class, new ObfuscatedIdSupport(obfuscator, invalidAllowed));
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    public static class ObfuscatedIdSupport extends PropertyEditorSupport implements Formatter<ObfuscatedId> {

        private final IdObfuscator obfuscator;

        @Getter
        private final boolean invalidAllowed;

        public String getAsText() {
            if (this.getValue() instanceof ObfuscatedId) {
                return ((ObfuscatedId) this.getValue()).getObfuscated();
            }
            return null;
        }

        public void setAsText(String text) throws IllegalArgumentException {
            try {
                this.setValue(obfuscator.decode(text));
            } catch (ObfuscatedDecodeException e) {
                if (isInvalidAllowed()) {
                    this.setValue(new SimpleObfuscatedId(null, text));
                }
            }
        }

        @Override
        public ObfuscatedId parse(String text, Locale locale) {
            try {
                return obfuscator.decode(text);
            } catch (ObfuscatedDecodeException e) {
                if (isInvalidAllowed()) {
                    return new SimpleObfuscatedId(null, text);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public String print(ObfuscatedId object, Locale locale) {
            return object != null ? object.getObfuscated() : null;
        }
    }

}
