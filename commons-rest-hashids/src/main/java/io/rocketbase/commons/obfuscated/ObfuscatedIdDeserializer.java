package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.util.StringUtils;

import java.io.IOException;

@JsonComponent
public class ObfuscatedIdDeserializer extends JsonDeserializer<ObfuscatedId> {

    private final IdObfuscator idObfuscator;
    private final boolean invalidAllowed;

    public ObfuscatedIdDeserializer(@Autowired IdObfuscator idObfuscator, @Value("${hashids.invalid.allowed:false}") boolean invalidAllowed) {
        this.idObfuscator = idObfuscator;
        this.invalidAllowed = invalidAllowed;
    }

    @Override
    public ObfuscatedId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
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
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }

}
