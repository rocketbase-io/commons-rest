package io.rocketbase.commons.translation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.annotation.PersistenceConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @_({@JsonCreator, @PersistenceConstructor}))
@JsonSerialize(using = TranslationSerializer.class)
@JsonDeserialize(using = TranslationDeserializer.class)
public class Translation implements Serializable {

    @HasDefaultLocale
    private Map<Locale, String> translations = new HashMap<>();

    public static Translation of(Locale locale, String value) {
        return new Translation().add(locale, value);
    }

    public static Translation translation(String translation) {
        return Translation.builder()
                .translation(LocaleContextHolder.getLocale(), translation)
                .build();
    }

    public static TranslationBuilder builder() {
        return new TranslationBuilder();
    }

    public Translation add(Locale locale, String value) {
        translations.put(locale, value);
        return this;
    }

    public Translation english(String value) {
        return add(Locale.ENGLISH, value);
    }

    public Translation french(String value) {
        return add(Locale.FRENCH, value);
    }

    public Translation german(String value) {
        return add(Locale.GERMAN, value);
    }

    public Translation italian(String value) {
        return add(Locale.ITALIAN, value);
    }

    public Translation japanese(String value) {
        return add(Locale.JAPANESE, value);
    }

    public Translation korean(String value) {
        return add(Locale.KOREAN, value);
    }

    public Translation chinese(String value) {
        return add(Locale.CHINESE, value);
    }

    public String getTranslated(Locale locale) {
        Locale language = Locale.forLanguageTag(locale.getLanguage());

        if (translations.containsKey(language)) {
            return translations.get(language);
        } else if (translations.containsKey(Locale.ENGLISH)) {
            return translations.get(Locale.ENGLISH);
        } else {
            if (translations.containsKey(LocaleContextHolder.getLocale())) {
                return translations.get(LocaleContextHolder.getLocale());
            }
        }
        return null;
    }

    public boolean hasLocale(Locale locale) {
        Locale language = Locale.forLanguageTag(locale.getLanguage());
        return translations.containsKey(language);
    }

    public String getTranslated() {
        return getTranslated(LocaleContextHolder.getLocale());
    }

    public static class TranslationBuilder {
        private Map<Locale, String> translations;

        TranslationBuilder() {
        }

        public Translation.TranslationBuilder translations(Map<Locale, String> translations) {
            this.translations = translations;
            return this;
        }


        public Translation.TranslationBuilder translation(Locale locale, String translation) {
            if (this.translations == null) {
                translations = new HashMap<>();
            }
            translations.put(locale, translation);
            return this;
        }

        public Translation build() {
            return new Translation(translations);
        }
    }
}
