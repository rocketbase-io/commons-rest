package io.rocketbase.commons.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class PageableResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SampleDto {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SampleMeta {
        private String summary;
        private int totalCount;
    }

    // ==================== PageableResultImpl Tests ====================

    @Test
    public void shouldDeserializePageableResultImpl() throws Exception {
        // given
        String json = "{\n" +
                "  \"content\": [\n" +
                "    {\"id\": 1, \"name\": \"Item 1\"},\n" +
                "    {\"id\": 2, \"name\": \"Item 2\"}\n" +
                "  ],\n" +
                "  \"page\": 0,\n" +
                "  \"pageSize\": 10,\n" +
                "  \"totalElements\": 2,\n" +
                "  \"totalPages\": 1\n" +
                "}";

        // when
        PageableResultImpl<SampleDto> result = mapper.readValue(json, new TypeReference<PageableResultImpl<SampleDto>>() {});

        // then
        assertThat(result, notNullValue());
        assertThat(result.content(), hasSize(2));
        assertThat(result.page(), equalTo(0));
        assertThat(result.pageSize(), equalTo(10));
        assertThat(result.totalElements(), equalTo(2L));
        assertThat(result.totalPages(), equalTo(1));
        assertThat(result.content().get(0).getId(), equalTo(1L));
        assertThat(result.content().get(0).getName(), equalTo("Item 1"));
    }

    @Test
    public void shouldSerializePageableResultImpl() throws Exception {
        // given
        List<SampleDto> content = Arrays.asList(
                new SampleDto(1L, "Item 1"),
                new SampleDto(2L, "Item 2")
        );
        PageableResultImpl<SampleDto> pageableResult = new PageableResultImpl<>(
                content,
                0,
                10,
                2,
                1
        );

        // when
        String json = mapper.writeValueAsString(pageableResult);

        // then
        assertThat(json, notNullValue());
        assertThat(json.contains("\"page\":0"), equalTo(true));
        assertThat(json.contains("\"pageSize\":10"), equalTo(true));
        assertThat(json.contains("\"totalElements\":2"), equalTo(true));
        assertThat(json.contains("\"totalPages\":1"), equalTo(true));
    }

    @Test
    public void shouldSerializeAndDeserializePageableResultImpl() throws Exception {
        // given
        List<SampleDto> content = Arrays.asList(
                new SampleDto(1L, "Item 1"),
                new SampleDto(2L, "Item 2")
        );
        PageableResultImpl<SampleDto> original = new PageableResultImpl<>(
                content,
                0,
                10,
                2,
                1
        );

        // when
        String json = mapper.writeValueAsString(original);
        PageableResultImpl<SampleDto> deserialized = mapper.readValue(json, new TypeReference<PageableResultImpl<SampleDto>>() {});

        // then
        assertThat(deserialized, notNullValue());
        assertThat(deserialized.content(), hasSize(2));
        assertThat(deserialized.page(), equalTo(original.page()));
        assertThat(deserialized.pageSize(), equalTo(original.pageSize()));
        assertThat(deserialized.totalElements(), equalTo(original.totalElements()));
        assertThat(deserialized.totalPages(), equalTo(original.totalPages()));
        assertThat(deserialized.content().get(0).getId(), equalTo(1L));
        assertThat(deserialized.content().get(0).getName(), equalTo("Item 1"));
    }

    // ==================== PageableResultWithMeta Tests ====================

    @Test
    public void shouldDeserializePageableResultWithMeta() throws Exception {
        // given
        String json = "{\n" +
                "  \"content\": [\n" +
                "    {\"id\": 1, \"name\": \"Item 1\"},\n" +
                "    {\"id\": 2, \"name\": \"Item 2\"}\n" +
                "  ],\n" +
                "  \"page\": 0,\n" +
                "  \"pageSize\": 10,\n" +
                "  \"totalElements\": 2,\n" +
                "  \"totalPages\": 1,\n" +
                "  \"meta\": {\n" +
                "    \"summary\": \"Test Summary\",\n" +
                "    \"totalCount\": 100\n" +
                "  }\n" +
                "}";

        // when
        PageableResultWithMeta<SampleDto, SampleMeta> result = mapper.readValue(json,
                new TypeReference<PageableResultWithMeta<SampleDto, SampleMeta>>() {});

        // then
        assertThat(result, notNullValue());
        assertThat(result.content(), hasSize(2));
        assertThat(result.page(), equalTo(0));
        assertThat(result.pageSize(), equalTo(10));
        assertThat(result.totalElements(), equalTo(2L));
        assertThat(result.totalPages(), equalTo(1));
        assertThat(result.meta(), notNullValue());
        assertThat(result.meta().getSummary(), equalTo("Test Summary"));
        assertThat(result.meta().getTotalCount(), equalTo(100));
    }

    @Test
    public void shouldDeserializePageableResultWithMetaNull() throws Exception {
        // given
        String json = "{\n" +
                "  \"content\": [\n" +
                "    {\"id\": 1, \"name\": \"Item 1\"}\n" +
                "  ],\n" +
                "  \"page\": 0,\n" +
                "  \"pageSize\": 10,\n" +
                "  \"totalElements\": 1,\n" +
                "  \"totalPages\": 1,\n" +
                "  \"meta\": null\n" +
                "}";

        // when
        PageableResultWithMeta<SampleDto, SampleMeta> result = mapper.readValue(json,
                new TypeReference<PageableResultWithMeta<SampleDto, SampleMeta>>() {});

        // then
        assertThat(result, notNullValue());
        assertThat(result.content(), hasSize(1));
        assertThat(result.meta(), nullValue());
    }

    @Test
    public void shouldSerializePageableResultWithMeta() throws Exception {
        // given
        List<SampleDto> content = Arrays.asList(
                new SampleDto(1L, "Item 1"),
                new SampleDto(2L, "Item 2")
        );
        SampleMeta meta = new SampleMeta("Test Summary", 100);
        PageableResultWithMeta<SampleDto, SampleMeta> pageableResult = new PageableResultWithMeta<>(
                content,
                0,
                10,
                2,
                1,
                meta
        );

        // when
        String json = mapper.writeValueAsString(pageableResult);

        // then
        assertThat(json, notNullValue());
        assertThat(json.contains("\"page\":0"), equalTo(true));
        assertThat(json.contains("\"pageSize\":10"), equalTo(true));
        assertThat(json.contains("\"totalElements\":2"), equalTo(true));
        assertThat(json.contains("\"totalPages\":1"), equalTo(true));
        assertThat(json.contains("\"meta\""), equalTo(true));
        assertThat(json.contains("\"summary\":\"Test Summary\""), equalTo(true));
        assertThat(json.contains("\"totalCount\":100"), equalTo(true));
    }

    @Test
    public void shouldSerializeAndDeserializePageableResultWithMeta() throws Exception {
        // given
        List<SampleDto> content = Arrays.asList(
                new SampleDto(1L, "Item 1"),
                new SampleDto(2L, "Item 2")
        );
        SampleMeta meta = new SampleMeta("Test Summary", 100);
        PageableResultWithMeta<SampleDto, SampleMeta> original = new PageableResultWithMeta<>(
                content,
                0,
                10,
                2,
                1,
                meta
        );

        // when
        String json = mapper.writeValueAsString(original);
        PageableResultWithMeta<SampleDto, SampleMeta> deserialized = mapper.readValue(json,
                new TypeReference<PageableResultWithMeta<SampleDto, SampleMeta>>() {});

        // then
        assertThat(deserialized, notNullValue());
        assertThat(deserialized.content(), hasSize(2));
        assertThat(deserialized.page(), equalTo(original.page()));
        assertThat(deserialized.pageSize(), equalTo(original.pageSize()));
        assertThat(deserialized.totalElements(), equalTo(original.totalElements()));
        assertThat(deserialized.totalPages(), equalTo(original.totalPages()));
        assertThat(deserialized.meta(), notNullValue());
        assertThat(deserialized.meta().getSummary(), equalTo("Test Summary"));
        assertThat(deserialized.meta().getTotalCount(), equalTo(100));
        assertThat(deserialized.content().get(0).getId(), equalTo(1L));
        assertThat(deserialized.content().get(0).getName(), equalTo("Item 1"));
    }
}