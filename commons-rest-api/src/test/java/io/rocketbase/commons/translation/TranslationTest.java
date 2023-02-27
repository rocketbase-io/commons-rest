package io.rocketbase.commons.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TranslationTest {

    @Test
    public void getTranslatedFallbackInCaseOfOnlyOne() {
        // given
        Translation translation = Translation.of(Locale.GERMAN, "Hallo");
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // when
        String result = translation.getTranslated();

        // then
        assertThat(result, equalTo("Hallo"));
    }

    @Test
    public void getTranslatedNullFallbackInCaseOfMany() {
        // given
        Translation translation = Translation.of(Locale.GERMAN, "Hallo")
                .italian("Ciao");
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // when
        String result = translation.getTranslated();

        // then
        assertThat(result, nullValue());
    }

    @Test
    public void getTranslatedRootFallbackInCaseOfMany() {
        // given
        Translation translation = Translation.of(Locale.GERMAN, "Hallo")
                .root("-_-");
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // when
        String result = translation.getTranslated();

        // then
        assertThat(result, equalTo("-_-"));
    }

    @Test
    public void getTranslatedExaktMatch() {
        // given
        Translation translation = Translation.of(Locale.GERMAN, "Hallo")
                .english("Hello");
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // when
        String result = translation.getTranslated();

        // then
        assertThat(result, equalTo("Hello"));
    }

    @Test
    public void getTranslatedLanguageMatch() {
        // given
        Translation translation = Translation.of(Locale.GERMAN, "Hallo")
                .add(Locale.forLanguageTag("en-uk"), "Hellö");
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // when
        String result = translation.getTranslated();

        // then
        assertThat(result, equalTo("Hellö"));
    }

    @Test
    public void equalsCheck() {
        // given
        Translation one = Translation.of(Locale.GERMAN, "Hallo")
                .add(Locale.forLanguageTag("en-uk"), "Hellö");
        Translation two = Translation.of(Locale.forLanguageTag("en-uk"), "Hellö")
                .add(Locale.GERMAN, "Hallo");


        // then
        assertThat(one.equals(two), equalTo(true));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void equalsCheckExtended() {
        // given
        Translation one = Translation.of(Locale.GERMAN, "Hallo")
                .add(Locale.forLanguageTag("en-uk"), "Hellö");

        Translation two = new Translation();

        Map<Locale, String> translations = new LinkedHashMap();
        translations.put(Locale.forLanguageTag("en-uk"), "Hellö");
        translations.put(Locale.GERMAN, "Hallo");
        two.setTranslations(translations);


        // then
        assertThat(one.equals(two), equalTo(true));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void equalsCheckNull() {
        // given
        Translation one = new Translation();

        Translation two = new Translation(null);


        // then
        assertThat(one.equals(two), equalTo(true));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void checkEqualsAfterSerialize() throws JsonProcessingException {
        // given
        Translation serialized = new ObjectMapper()
                .readValue("{\"en\":\"blue\", \"de\":\"blau\"}", Translation.class);
        Translation crafted = Translation.of(Locale.GERMAN, "blau")
                .english("blue");

        // then
        assertThat(serialized.equals(crafted), equalTo(true));
        assertThat(serialized.hashCode(), equalTo(crafted.hashCode()));
    }

    @Test
    public void checkToString() {
        // given
        Translation sample = Translation.of(Locale.GERMAN, "Hallo bla=blub")
                .english("Hello bla=blub");


        // then
        assertThat(sample.toString(), equalTo("Translation({de=Hallo bla=blub, en=Hello bla=blub})"));
    }
}
