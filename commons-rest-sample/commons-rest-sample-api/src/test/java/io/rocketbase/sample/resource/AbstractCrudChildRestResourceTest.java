package io.rocketbase.sample.resource;


import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.resource.AbstractCrudChildRestResource;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;


public class AbstractCrudChildRestResourceTest {

    @Test
    public void buildBaseUriBuilderWithoutSlash() {
        // given
        TestWithoutSlashCrudChildRestResource resoure = new TestWithoutSlashCrudChildRestResource();

        // when
        UriComponentsBuilder builder = resoure.buildBaseUriBuilder("123");

        // then
        MatcherAssert.assertThat(builder, CoreMatchers.notNullValue());
        MatcherAssert.assertThat(builder.toUriString(), CoreMatchers.equalTo(TestWithoutSlashCrudChildRestResource.BASE_PARENT_API_URL + "/123/child/"));
    }

    @Test
    public void buildBaseUriBuilderWithSlash() {
        // given
        TestWithSlashCrudChildRestResource resoure = new TestWithSlashCrudChildRestResource();

        // when
        UriComponentsBuilder builder = resoure.buildBaseUriBuilder("123");

        // then
        MatcherAssert.assertThat(builder, CoreMatchers.notNullValue());
        MatcherAssert.assertThat(builder.toUriString(), CoreMatchers.equalTo("https://localhost:8080/api/parent/123/child/"));
    }

    private class TestWithoutSlashCrudChildRestResource extends AbstractCrudChildRestResource<Object, Object, String> {

        public static final String BASE_PARENT_API_URL = "https://localhost:8080/api/parent";


        @Override
        protected String getBaseParentApiUrl() {
            return BASE_PARENT_API_URL;
        }

        @Override
        protected String getChildPath() {
            return "child";
        }

        @Override
        protected ParameterizedTypeReference<PageableResult<Object>> createPagedTypeReference() {
            return new ParameterizedTypeReference<PageableResult<Object>>() {
            };
        }
    }


    private class TestWithSlashCrudChildRestResource extends AbstractCrudChildRestResource<Object, Object, String> {


        @Override
        protected String getBaseParentApiUrl() {
            return "https://localhost:8080/api/parent/";
        }

        @Override
        protected String getChildPath() {
            return "/child/";
        }

        @Override
        protected ParameterizedTypeReference<PageableResult<Object>> createPagedTypeReference() {
            return new ParameterizedTypeReference<PageableResult<Object>>() {
            };
        }
    }

}
