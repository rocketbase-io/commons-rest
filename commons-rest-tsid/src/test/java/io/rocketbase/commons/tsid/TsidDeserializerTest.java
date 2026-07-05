package io.rocketbase.commons.tsid;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.BaseTest;
import io.rocketbase.commons.exception.TsidDecodeException;
import jakarta.annotation.Resource;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TsidDeserializerTest extends BaseTest {

    @Resource
    private ObjectMapper mapper;

    @Test
    void deserialize() throws Exception {
        // given
        TSID id = TSID.Factory.getTsid();
        String value = "{ \"id\": \"" + id.toString() + "\", \"name\": \"test\" }";

        // when
        TestIdName result = mapper.readValue(value, TestIdName.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getName(), equalTo("test"));
        assertThat(result.getId().getUnixMilliseconds(), equalTo(id.getUnixMilliseconds()));
        assertThat(result.getId().toLong(), equalTo(id.toLong()));
    }

    @Test
    void deserializeInvalidThrowsByDefault() {
        // given - default strict module
        ObjectMapper strictMapper = JsonMapper.builder().addModule(new TsidModule()).build();

        // when / then
        assertThrows(Exception.class,
                () -> strictMapper.readValue("{ \"id\": \"not-a-tsid!\", \"name\": \"test\" }", TestIdName.class));
    }

    @Test
    void deserializeInvalidReturnsNullWhenAllowed() {
        // given - lenient module (tsid.invalid.allowed=true)
        ObjectMapper lenientMapper = JsonMapper.builder().addModule(new TsidModule(true)).build();

        // when
        TestIdName result = lenientMapper.readValue("{ \"id\": \"not-a-tsid!\", \"name\": \"test\" }", TestIdName.class);

        // then
        assertThat(result.getId(), nullValue());
        assertThat(result.getName(), equalTo("test"));
    }

    @Test
    void deserializeBlankReturnsNull() {
        // given
        ObjectMapper strictMapper = JsonMapper.builder().addModule(new TsidModule()).build();

        // when - blank is "absent", not invalid
        TestIdName result = strictMapper.readValue("{ \"id\": \"\", \"name\": \"test\" }", TestIdName.class);

        // then
        assertThat(result.getId(), nullValue());
    }

    @Test
    void converterMirrorsInvalidHandling() {
        assertThat(new TsidConverter().convert(""), nullValue());
        assertThrows(TsidDecodeException.class, () -> new TsidConverter().convert("not-a-tsid!"));
        assertThat(new TsidConverter(true).convert("not-a-tsid!"), nullValue());
    }

    @Data
    private static class TestIdName {
        private TSID id;
        private String name;
    }
}