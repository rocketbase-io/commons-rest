package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.util.StringUtils;

import java.io.IOException;

@JsonComponent
@RequiredArgsConstructor
public class ObfuscatedIdDeserializer extends JsonDeserializer<ObfuscatedId> {

    private final IdObfuscator idObfuscator;

    @Override
    public ObfuscatedId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        String value = jsonParser.getValueAsString();
        if (!StringUtils.isEmpty(value)) {
            try {
                return idObfuscator.decode(value);
            } catch (ObfuscatedDecodeException ignore) {
            }
        }
        return null;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }

}
