package io.rocketbase.commons.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class TranslationSerializerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldSerializeTranslation() throws Exception {
        // given
        Map<Locale, String> map = new HashMap<>();
        map.put(Locale.GERMAN, "deutsch");
        map.put(Locale.ENGLISH, "english");

        Translation translation = Translation.builder()
                .translations(map)
                .build();

        // when
        String result = mapper.writeValueAsString(translation);

        // then
        assertThat(result, equalTo("{\"de\":\"deutsch\",\"en\":\"english\"}"));
    }

    @Test
    public void shouldSerializeWithRegion() throws Exception {
        // given
        Map<Locale, String> map = new HashMap<>();
        map.put(Locale.GERMANY, "deutsch");
        map.put(Locale.US, "english");

        Translation translation = Translation.builder()
                .translations(map)
                .build();

        // when
        String result = mapper.writeValueAsString(translation);

        // then
        assertThat(result, equalTo("{\"en-US\":\"english\",\"de-DE\":\"deutsch\"}"));
    }

    @Test
    public void shouldSerializeWithRegionAndVariant() throws Exception {
        // given
        Map<Locale, String> map = new HashMap<>();
        map.put(new Locale("de", "DE", "bayrisch"), "deitsch");
        map.put(Locale.US, "english");

        Translation translation = Translation.builder()
                .translations(map)
                .build();

        // when
        String result = mapper.writeValueAsString(translation);

        // then
        assertThat(result, equalTo("{\"de-DE-bayrisch\":\"deitsch\",\"en-US\":\"english\"}"));
    }

    @Test
    public void shouldSerializeRootCorrectly() throws Exception {
        // given
        Map<Locale, String> map = new HashMap<>();
        map.put(Locale.ROOT, "root");
        map.put(Locale.US, "us");

        Translation translation = Translation.builder()
                .translations(map)
                .build();

        // when
        String result = mapper.writeValueAsString(translation);

        // then
        assertThat(result, equalTo("{\"und\":\"root\",\"en-US\":\"us\"}"));
    }

    @Test
    public void shouldSerializeInObject() throws Exception {
        // give
        WrappedObject wrappedObject = WrappedObject.builder()
                .id("123")
                .name(new Translation()
                        .german("Hallo")
                        .english("Hello"))
                .build();
        // when
        String result = mapper.writeValueAsString(wrappedObject);

        // then
        assertThat(result, equalTo("{\"id\":\"123\",\"name\":{\"de\":\"Hallo\",\"en\":\"Hello\"}}"));
    }

    @Test
    public void shouldSerializeInObjectWithNull() throws Exception {
        // give
        WrappedObject wrappedObject = WrappedObject.builder()
                .id("123")
                .name(null)
                .build();
        // when
        String result = mapper.writeValueAsString(wrappedObject);

        // then
        assertThat(result, equalTo("{\"id\":\"123\",\"name\":null}"));
    }

    @Test
    public void shouldDetectAnnotation() throws Exception {
        // given
        WrappedObjectTranslated wrappedObject = WrappedObjectTranslated.builder()
                .id("123")
                .name(new Translation()
                        .german("Hallo")
                        .english("Hello"))
                .build();

        // when
        LocaleContextHolder.setLocale(Locale.GERMAN);
        String result = mapper.writeValueAsString(wrappedObject);

        // then
        assertThat(result, equalTo("{\"id\":\"123\",\"name\":\"Hallo\"}"));
    }

    @Test
    public void shouldDetectAnnotationWithLocale() throws Exception {
        // given
        WrappedObjectTranslatedWithLocale wrappedObject = WrappedObjectTranslatedWithLocale.builder()
                .id("123")
                .name(new Translation()
                        .german("Hallo")
                        .english("Hello"))
                .build();

        // when
        LocaleContextHolder.setLocale(Locale.GERMAN);
        String result = mapper.writeValueAsString(wrappedObject);

        // then
        assertThat(result, equalTo("{\"id\":\"123\",\"name\":\"Hello\"}"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WrappedObject {
        private String id;

        private Translation name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WrappedObjectTranslated {
        private String id;

        @Translated
        private Translation name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WrappedObjectTranslatedWithLocale {
        private String id;

        @Translated("en")
        private Translation name;
    }

}