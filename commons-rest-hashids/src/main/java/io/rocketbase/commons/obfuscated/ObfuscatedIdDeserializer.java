package io.rocketbase.commons.obfuscated;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.util.StringUtils;

@JacksonComponent
public class ObfuscatedIdDeserializer extends ValueDeserializer<ObfuscatedId> {

    private final IdObfuscator idObfuscator;
    private final boolean invalidAllowed;

    public ObfuscatedIdDeserializer(@Autowired IdObfuscator idObfuscator, @Value("${hashids.invalid.allowed:false}") boolean invalidAllowed) {
        this.idObfuscator = idObfuscator;
        this.invalidAllowed = invalidAllowed;
    }

    @Override
    public ObfuscatedId deserialize(JsonParser jsonParser, DeserializationContext ctxt) {
        String value = jsonParser.getValueAsString();
        if (StringUtils.hasText(value)) {
            try {
                return idObfuscator.decode(value);
            } catch (ObfuscatedDecodeException ignore) {
            }
        }
        if (invalidAllowed) {
            return new SimpleObfuscatedId(null, value);
        } else {
            return null;
        }
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) {
        return deserialize(p, ctxt);
    }

}
