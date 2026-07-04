package io.rocketbase.commons.translation;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TranslationDeserializer extends ValueDeserializer<Translation> {

    @Override
    public Translation deserialize(JsonParser jsonParser, DeserializationContext ctxt) {
        Map<Locale, String> translations = new HashMap<>();

        String language = null;
        JsonToken currentToken = jsonParser.currentToken();
        if (currentToken.equals(JsonToken.VALUE_STRING)) {
            return Translation.translation(jsonParser.getString());
        }
        while (currentToken != JsonToken.END_OBJECT) {
            currentToken = jsonParser.nextToken();
            if (currentToken == JsonToken.PROPERTY_NAME) {
                language = jsonParser.currentName();
            } else if (currentToken == JsonToken.VALUE_STRING) {
                translations.put(parseLanguageTag(language), jsonParser.getString());
            }
        }
        return Translation.builder()
                .translations(translations)
                .build();
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) {
        return deserialize(p, ctxt);
    }

    /**
     * in case locale has been parsed via toString (that uses _ as separator instead of -)
     */
    protected Locale parseLanguageTag(String language) {
        if (language == null) {
            return null;
        }
        return Locale.forLanguageTag(language.replace("_", "-"));
    }
}
