package io.rocketbase.commons.config;

import io.rocketbase.commons.controller.exceptionhandler.BeanValidationExceptionHandler;
import io.rocketbase.commons.controller.exceptionhandler.ObfuscatedDecodeExceptionHandler;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import io.rocketbase.commons.service.DefaultIdObfuscator;
import io.rocketbase.commons.obfuscated.IdObfuscator;
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
import java.text.ParseException;
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
    @ConditionalOnProperty(name = "handler.obfuscatedDecode.enabled", matchIfMissing = true)
    public ObfuscatedDecodeExceptionHandler obfuscatedDecodeExceptionHandler() {
        return new ObfuscatedDecodeExceptionHandler();
    }

    @RestControllerAdvice
    public static class Advice {

        @Autowired
        IdObfuscator obfuscator;

        @InitBinder
        public void addSupportForObfuscatedId(WebDataBinder binder) {
            binder.registerCustomEditor(ObfuscatedId.class, new ObfuscatedIdSupport(obfuscator));
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    public static class ObfuscatedIdSupport extends PropertyEditorSupport implements Formatter<ObfuscatedId> {

        private final IdObfuscator obfuscator;

        public static final void registerCustomEditor(final WebDataBinder binder, final IdObfuscator obfuscator) {
            if (binder == null) throw new IllegalArgumentException();
            if (obfuscator == null) throw new IllegalArgumentException();

            Class<ObfuscatedId> theClazz = ObfuscatedId.class;
            ObfuscatedIdSupport propertyEditor = new ObfuscatedIdSupport(obfuscator);

            binder.registerCustomEditor(theClazz, propertyEditor);
            log.debug("Registered {} to support parameters of type {}.", propertyEditor, theClazz);
        }

        public String getAsText() {
            if (this.getValue() instanceof ObfuscatedId) {
                return ((ObfuscatedId) this.getValue()).getObfuscated();
            }
            return null;
        }

        public void setAsText(String text) throws IllegalArgumentException {
            this.setValue(obfuscator.decode(text));
        }

        @Override
        public ObfuscatedId parse(String text, Locale locale) throws ParseException {
            return obfuscator.decode(text);
        }

        @Override
        public String print(ObfuscatedId object, Locale locale) {
            return object.getObfuscated();
        }
    }

}
