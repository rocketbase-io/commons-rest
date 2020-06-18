package io.rocketbase.commons.translation;

import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
}