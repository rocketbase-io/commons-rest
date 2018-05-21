package io.rocketbase.commons.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

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
    }

}