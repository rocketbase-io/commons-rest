package io.rocketbase.commons.translation;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import io.rocketbase.commons.util.Nulls;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class TranslationSerializer extends ValueSerializer<Translation> {

    protected final TranslationSerializerConfig config;

    public TranslationSerializer() {
        this(new TranslationSerializerConfig(false));
    }

    @Override
    public void serialize(Translation value, JsonGenerator jsonGenerator, SerializationContext serializers) {
        if (config.isTranslated()) {
            writeTranslated(value, jsonGenerator);
        } else {
            writeObject(value, jsonGenerator);
        }
    }

    protected void writeTranslated(Translation value, JsonGenerator jsonGenerator) {
        String valueTranslated = value.getTranslated(Nulls.notNull(config.getLocale(), LocaleContextHolder.getLocale()));
        if (valueTranslated != null) {
            jsonGenerator.writeString(valueTranslated);
        } else {
            jsonGenerator.writeNull();
        }
    }

    protected void writeObject(Translation value, JsonGenerator jsonGenerator) {
        jsonGenerator.writeStartObject();
        for (Map.Entry<Locale, String> entry : value.getTranslations()
                .entrySet()) {
            jsonGenerator.writeStringProperty(entry.getKey().toLanguageTag(), entry.getValue());
        }
        jsonGenerator.writeEndObject();
    }

    @Override
    public void serializeWithType(Translation value, JsonGenerator jsonGenerator, SerializationContext serializers, TypeSerializer typeSer) {
        serialize(value, jsonGenerator, serializers);
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext serializerProvider, BeanProperty beanProperty) {
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
