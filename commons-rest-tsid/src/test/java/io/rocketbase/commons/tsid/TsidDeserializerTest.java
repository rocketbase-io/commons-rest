package io.rocketbase.commons.tsid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.BaseTest;
import jakarta.annotation.Resource;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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

    @Data
    private static class TestIdName {
        private TSID id;
        private String name;
    }
}