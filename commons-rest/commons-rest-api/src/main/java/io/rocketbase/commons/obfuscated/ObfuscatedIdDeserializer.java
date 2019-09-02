package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class ObfuscatedIdDeserializer extends JsonDeserializer<ObfuscatedId> {

    @Override
    public ObfuscatedId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        String value = jsonParser.getValueAsString();
        if (StringUtils.isEmpty(value)) {
            return new SimpleObfuscatedId(null, value);
        }
        return null;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }
}
