package io.rocketbase.commons.translation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class TranslationSerializer extends JsonSerializer<Translation> {

    @Override
    public void serialize(Translation value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
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

}
