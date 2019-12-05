package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.BaseTest;
import lombok.Data;
import org.junit.Test;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObfuscatedIdDeserializerTest extends BaseTest {

    @Resource
    private ObjectMapper mapper;

    @Resource
    private IdObfuscator idObfuscator;

    @Test
    public void shouldDeserialize() throws Exception {
        // given
        ObfuscatedId obfuscate = idObfuscator.obfuscate(1L);
        String value = "{ \"id\": \"" + obfuscate.getObfuscated() + "\", \"name\": \"test\" }";

        // when
        TestIdName result = mapper.readValue(value, TestIdName.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getName(), equalTo("test"));
        assertThat(result.getId().getObfuscated(), equalTo(obfuscate.getObfuscated()));
        assertThat(result.getId().getId(), equalTo(obfuscate.getId()));
    }

    @Data
    private static class TestIdName {
        private ObfuscatedId id;
        private String name;
    }
}