package io.rocketbase.commons.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

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

}