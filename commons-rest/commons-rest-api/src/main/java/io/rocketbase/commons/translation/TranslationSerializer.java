package io.rocketbase.commons.translation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.rocketbase.commons.util.Nulls;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class TranslationSerializer extends JsonSerializer<Translation> implements ContextualSerializer {

    protected final TranslationSerializerConfig config;

    public TranslationSerializer() {
        this(new TranslationSerializerConfig(false));
    }

    @Override
    public void serialize(Translation value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        if (config.isTranslated()) {
            writeTranslated(value, jsonGenerator);
        } else {
            writeObject(value, jsonGenerator);
        }
    }

    protected void writeTranslated(Translation value, JsonGenerator jsonGenerator) throws IOException {
        String valueTranslated = value.getTranslated(Nulls.notNull(config.getLocale(), LocaleContextHolder.getLocale()));
        if (valueTranslated != null) {
            jsonGenerator.writeString(valueTranslated);
        } else {
            jsonGenerator.writeNull();
        }
    }

    protected void writeObject(Translation value, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        for (Map.Entry<Locale, String> entry : value.getTranslations()
                .entrySet()) {
            jsonGenerator.writeStringField(entry.getKey().toLanguageTag(), entry.getValue());
        }
        jsonGenerator.writeEndObject();
    }

    @Override
    public void serializeWithType(Translation value, JsonGenerator jsonGenerator, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        serialize(value, jsonGenerator, serializers);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        try {
            Translated annotation = beanProperty.getAnnotation(Translated.class);
            if (annotation != null) {
                TranslationSerializerConfig config = new TranslationSerializerConfig(true);
                if (StringUtils.hasText(annotation.value())) {
                    config.setLocale(Locale.forLanguageTag(annotation.value()));
                }
                return new TranslationSerializer(config);
            }
        } catch (Exception e) {
        }
        return new TranslationSerializer();
    }

    @Data
    protected static class TranslationSerializerConfig {
        private boolean translated;
        private Locale locale;

        public TranslationSerializerConfig(boolean translated) {
            this.translated = translated;
        }

    }
}
