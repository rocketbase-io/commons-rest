package io.rocketbase.commons.dto.translation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TranslationDeserializer extends JsonDeserializer<Translation> {
    @Override
    public Translation deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        Map<Locale, String> translations = new HashMap<>();

        String language = null;
        JsonToken currentToken = jsonParser.getCurrentToken();
        while (currentToken != JsonToken.END_OBJECT) {
            currentToken = jsonParser.nextToken();
            if (currentToken == JsonToken.FIELD_NAME) {
                language = jsonParser.getText();
            } else if (currentToken == JsonToken.VALUE_STRING) {
                translations.put(Locale.forLanguageTag(language), jsonParser.getText());
            }
        }
        return Translation.builder()
                .translations(translations)
                .build();
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }
}
