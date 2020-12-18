package io.rocketbase.commons.resource;

import io.rocketbase.commons.dto.PageableResult;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbstractCrudRestResourceTest {

    @Test
    public void buildBaseUriBuilder() {
        // given
        TestWithoutSlashCrudRestResource resoure = new TestWithoutSlashCrudRestResource();

        // when
        UriComponentsBuilder builder = resoure.buildBaseUriBuilder();

        // then
        assertThat(builder, notNullValue());
        assertThat(builder.toUriString(), equalTo(TestWithoutSlashCrudRestResource.BASE_PARENT_API_URL + "/"));
    }

    private class TestWithoutSlashCrudRestResource extends AbstractCrudRestResource<Object, Object, String> {

        public static final String BASE_PARENT_API_URL = "https://localhost:8080/api/entity";

        @Override
        protected ParameterizedTypeReference<PageableResult<Object>> createPagedTypeReference() {
            return new ParameterizedTypeReference<PageableResult<Object>>() {
            };
        }

        @Override
        protected String getBaseApiUrl() {
            return BASE_PARENT_API_URL;
        }
    }

}