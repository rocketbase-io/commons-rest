package io.rocketbase.commons.tsid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.BaseTest;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class TsidSerializerTest extends BaseTest {

    @Resource
    private ObjectMapper mapper;

    @Test
    void serialize() throws Exception {
        // given
        TSID id = TSID.Factory.getTsid();
        TestIdName object = new TestIdName(id, "name");

        // when
        String result = mapper.writeValueAsString(object);

        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("{\"id\":\"" + id.toString() + "\",\"name\":\"name\"}"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestIdName {
        private TSID id;
        private String name;
    }
}