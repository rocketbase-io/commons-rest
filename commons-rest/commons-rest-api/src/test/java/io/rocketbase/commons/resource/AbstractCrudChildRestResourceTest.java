package io.rocketbase.commons.resource;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbstractCrudChildRestResourceTest {

    @Test
    public void buildBaseUriBuilderWithoutSlash() {
        // given
        TestWithoutSlashCrudChildRestResource resoure = new TestWithoutSlashCrudChildRestResource();

        // when
        UriComponentsBuilder builder = resoure.buildBaseUriBuilder("123");

        // then
        assertThat(builder, notNullValue());
        assertThat(builder.toUriString(), equalTo(TestWithoutSlashCrudChildRestResource.BASE_PARENT_API_URL + "/123/child/"));
    }

    @Test
    public void buildBaseUriBuilderWithSlash() {
        // given
        TestWithSlashCrudChildRestResource resoure = new TestWithSlashCrudChildRestResource();

        // when
        UriComponentsBuilder builder = resoure.buildBaseUriBuilder("123");

        // then
        assertThat(builder, notNullValue());
        assertThat(builder.toUriString(), equalTo("https://localhost:8080/api/parent/123/child/"));
    }

    private class TestWithoutSlashCrudChildRestResource extends AbstractCrudChildRestResource<Object, Object, String> {

        public static final String BASE_PARENT_API_URL = "https://localhost:8080/api/parent";

        public TestWithoutSlashCrudChildRestResource() {
            super(new ObjectMapper());
        }

        @Override
        protected String getBaseParentApiUrl() {
            return BASE_PARENT_API_URL;
        }

        @Override
        protected String getChildPath() {
            return "child";
        }

        @Override
        protected TypeReference<PageableResult<Object>> createPagedTypeReference() {
            return new TypeReference<PageableResult<Object>>() {
            };
        }
    }


    private class TestWithSlashCrudChildRestResource extends AbstractCrudChildRestResource<Object, Object, String> {

        public TestWithSlashCrudChildRestResource() {
            super(new ObjectMapper());
        }

        @Override
        protected String getBaseParentApiUrl() {
            return "https://localhost:8080/api/parent/";
        }

        @Override
        protected String getChildPath() {
            return "/child/";
        }

        @Override
        protected TypeReference<PageableResult<Object>> createPagedTypeReference() {
            return new TypeReference<PageableResult<Object>>() {
            };
        }
    }

}