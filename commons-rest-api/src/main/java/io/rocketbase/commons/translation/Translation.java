package io.rocketbase.commons.translation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.rocketbase.commons.util.LocaleFilter;
import io.rocketbase.commons.util.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = TranslationSerializer.class)
@JsonDeserialize(using = TranslationDeserializer.class)
@Schema(description = "holds translations in multiple languages")
public class Translation implements Serializable {

    @Getter
    @Setter
    @HasDefaultLocale
    private Map<Locale, String> translations = new HashMap<>();

    /**
     * init Translation
     *
     * @param locale Locale of value
     * @param value  text
     * @return new Translation instant
     */
    public static Translation of(Locale locale, String value) {
        return new Translation().add(locale, value);
    }

    /**
     * init Translation with value for Locale of LocaleContextHolder
     *
     * @param translation value
     * @return new Translation instant
     */
    public static Translation translation(String translation) {
        return Translation.builder()
                .translation(LocaleContextHolder.getLocale(), translation)
                .build();
    }

    public static TranslationBuilder builder() {
        return new TranslationBuilder();
    }

    /**
     * add translation to list - overwrite existing
     */
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

    public Translation root(String value) {
        return add(Locale.ROOT, value);
    }

    public Set<Locale> getLocales() {
        return translations.keySet();
    }

    /**
     * lookup for locale in following order:
     * <ul>
     *     <li>lookup for exact locale (de-DE-bayrisch)</li>
     *     <li>afterwards for only parts of the locale-tag (de-DE, de)</li>
     *     <li>afterwards fpr (en)</li>
     *     <li>then for (ROOT "")</li>
     *     <li>when only one translation exists return it</li>
     * </ul>
     *
     * @param locale exact match locale
     * @return translated value when found
     */
    @Nullable
    public String getTranslated(Locale locale) {
        Entry<Locale, String> foundEntry = LocaleFilter.findClosest(locale, translations, Locale.ENGLISH);

        if (foundEntry != null) {
            return foundEntry.getValue();
        } else {
            if (translations.containsKey(LocaleContextHolder.getLocale())) {
                foundEntry = LocaleFilter.findClosest(LocaleContextHolder.getLocale(), translations);
                return foundEntry != null ? foundEntry.getValue() : null;
            }
        }
        if (getLocales().contains(Locale.ROOT) && Locale.ROOT.equals(locale)) {
            return getTranslated(Locale.ROOT);
        } else if (translations.size() == 1) {
            return translations.values().iterator().next();
        }
        return null;
    }

    /**
     * lookup with locale of LocaleContextHolder
     *
     * @return translated value when found
     */
    @Nullable
    public String getTranslated() {
        return getTranslated(LocaleContextHolder.getLocale());
    }

    /**
     * exactly search within map if locale contains
     */
    public boolean hasLocale(Locale locale) {
        return translations.containsKey(locale);
    }

    /**
     * uses the {@link LocaleFilter} function to search if locale could be found
     */
    public boolean hasLocaleLooselyFilter(Locale locale) {
        return LocaleFilter.findClosest(locale, translations) != null;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Translation)) return false;
        final Translation other = (Translation) o;

        Map<Locale, String> oTrans = Nulls.notNull(getTranslations());
        Map<Locale, String> otherTrans = Nulls.notNull(other.getTranslations());

        if (oTrans.isEmpty() && otherTrans.isEmpty()) {
            return true;
        }
        if (!oTrans.keySet().equals(otherTrans.keySet())) {
            return false;
        }
        for (Entry<Locale, String> entry : oTrans.entrySet()) {
            if (!entry.getValue().equals(oTrans.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (Nulls.notNull(getTranslations()).hashCode());
        return result;
    }

    public String toString() {
        return "Translation(" + this.getTranslations() + ")";
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
