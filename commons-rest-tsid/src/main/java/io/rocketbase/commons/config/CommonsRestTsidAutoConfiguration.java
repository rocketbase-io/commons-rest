package io.rocketbase.commons.config;

import com.fasterxml.jackson.databind.Module;
import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.controller.exceptionhandler.TsidDecodeExceptionHandler;
import io.rocketbase.commons.exception.TsidDecodeException;
import io.rocketbase.commons.tsid.TsidConverter;
import io.rocketbase.commons.tsid.TsidModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.beans.PropertyEditorSupport;
import java.util.Locale;

@Configuration
public class CommonsRestTsidAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "tsid.handler.enabled", matchIfMissing = true)
    public TsidDecodeExceptionHandler tsidDecodeExceptionHandler() {
        return new TsidDecodeExceptionHandler();
    }

    @Bean
    public Module tsidModule() {
        return new TsidModule();
    }

    @Bean
    public Converter tsidConverter() {
        return new TsidConverter();
    }

    @RestControllerAdvice
    public static class Advice {

        @InitBinder
        public void addSupportForObfuscatedId(WebDataBinder binder) {
            binder.registerCustomEditor(TSID.class, new TSIDSupport());
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    public static class TSIDSupport extends PropertyEditorSupport implements Formatter<TSID> {


        public String getAsText() {
            if (this.getValue() instanceof TSID) {
                return ((TSID) this.getValue()).toString();
            }
            return null;
        }

        public void setAsText(String text) throws IllegalArgumentException {
            if (StringUtils.hasText(text) && TSID.isValid(text)) {
                this.setValue(TSID.from(text));
            } else {
                throw new TsidDecodeException();
            }
        }

        @Override
        public TSID parse(String text, Locale locale) {
            if (StringUtils.hasText(text) && TSID.isValid(text)) {
                return TSID.from(text);
            }
            throw new TsidDecodeException();
        }

        @Override
        public String print(TSID object, Locale locale) {
            return object != null ? object.toString() : null;
        }
    }

}
