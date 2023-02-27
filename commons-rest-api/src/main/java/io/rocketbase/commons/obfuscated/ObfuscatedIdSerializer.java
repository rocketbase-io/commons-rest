package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

public class ObfuscatedIdSerializer extends JsonSerializer<ObfuscatedId> {

    @Override
    public void serialize(ObfuscatedId value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        if (value != null) {
            jsonGenerator.writeString(value.getObfuscated());
        } else {
            jsonGenerator.writeNull();
        }
    }

    @Override
    public void serializeWithType(ObfuscatedId value, JsonGenerator jsonGenerator, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        serialize(value, jsonGenerator, serializers);
    }

}
