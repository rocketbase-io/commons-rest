package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObfuscatedIdSerializerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldSerializeTranslation() throws Exception {
        // given
        TestIdName testData = new TestIdName(new SimpleObfuscatedId(1L, "abc"), "test");
        // when
        String result = mapper.writeValueAsString(testData);

        // then
        assertThat(result, equalTo("{\"id\":\"abc\",\"name\":\"test\"}"));
    }

    @Data
    @AllArgsConstructor
    private static class TestIdName {
        private ObfuscatedId id;
        private String name;
    }

}