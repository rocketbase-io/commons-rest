package io.rocketbase.commons.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TranslationDeserializerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldDeserializeTranslation() throws Exception {
        // given
        String value = "{\"de\":\"deutsch\",\"en\":\"english\"}";

        // when
        Translation result = mapper.readValue(value, Translation.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTranslations(), notNullValue());
        assertThat(result.getTranslations().size(), equalTo(2));
        assertThat(result.getTranslations().containsKey(Locale.GERMAN), equalTo(true));
    }

    @Test
    public void shouldHandleLanguageWithRegion() throws Exception {
        // given
        String value = "{\"en-US\":\"english\",\"de-AT\":\"österreichisch\"}";

        // when
        Translation result = mapper.readValue(value, Translation.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTranslations(), notNullValue());
        assertThat(result.getTranslations().size(), equalTo(2));
        assertThat(result.getTranslations().containsKey(Locale.US), equalTo(true));
        assertThat(result.getTranslations().containsKey(Locale.ENGLISH), equalTo(false));
        assertThat(result.getTranslations().containsKey(new Locale("de", "AT")), equalTo(true));
        assertThat(result.getTranslations().containsKey(Locale.GERMAN), equalTo(false));
    }

    @Test
    public void shouldWorkWithToStringOfLocale() throws Exception {
        // given
        String value = "{\"de_DE\":\"english\"}";

        // when
        Translation result = mapper.readValue(value, Translation.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTranslations(), notNullValue());
        assertThat(result.getTranslations().size(), equalTo(1));
        assertThat(result.getTranslations().containsKey(Locale.GERMANY), equalTo(true));
    }

}