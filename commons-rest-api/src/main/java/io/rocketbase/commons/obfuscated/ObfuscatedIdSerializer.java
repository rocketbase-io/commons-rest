package io.rocketbase.commons.obfuscated;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;

public class ObfuscatedIdSerializer extends ValueSerializer<ObfuscatedId> {

    @Override
    public void serialize(ObfuscatedId value, JsonGenerator jsonGenerator, SerializationContext serializers) {
        if (value != null) {
            jsonGenerator.writeString(value.getObfuscated());
        } else {
            jsonGenerator.writeNull();
        }
    }

    @Override
    public void serializeWithType(ObfuscatedId value, JsonGenerator jsonGenerator, SerializationContext serializers, TypeSerializer typeSer) {
        serialize(value, jsonGenerator, serializers);
    }

}
