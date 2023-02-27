package io.rocketbase.commons.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
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

    @Test
    public void shouldWorkWithString() throws Exception {
        // given
        String value = "{\"id\":\"123\",\"name\":\"Hello\"}";

        // when
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        WrappedObject result = mapper.readValue(value, WrappedObject.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getName().getTranslations(), notNullValue());
        assertThat(result.getName().getTranslations().size(), equalTo(1));
        assertThat(result.getName().getTranslations().containsKey(Locale.ENGLISH), equalTo(true));
        assertThat(result.getName().getTranslations().get(Locale.ENGLISH), equalTo("Hello"));
    }

    @Test
    public void shouldWorkWithNull() throws Exception {
        // given
        String value = "{\"id\":\"123\",\"name\":null}";

        // when
        WrappedObject result = mapper.readValue(value, WrappedObject.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getName(), nullValue());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WrappedObject {
        private String id;

        private Translation name;
    }

}
