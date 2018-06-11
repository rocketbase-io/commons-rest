package io.rocketbase.commons.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractBaseCrudRestResource<Read, Write> extends AbstractRestResource {

    @Getter
    protected Class<Read> responseClass;

    @Setter
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public AbstractBaseCrudRestResource(ObjectMapper objectMapper) {
        super(objectMapper);
        responseClass = (Class<Read>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new BasicResponseErrorHandler());
        }
        return restTemplate;
    }

    @SneakyThrows
    protected PageableResult<Read> find(UriComponentsBuilder uriBuilder) {
        ResponseEntity<String> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(createHeaderWithLanguage()),
                String.class);
        return renderResponse(response, createPagedTypeReference());
    }

    /**
     * @param uriBuilder complete uri
     * @return object or null in case of not found (will catch {@link io.rocketbase.commons.exception.NotFoundException})
     */
    @SneakyThrows
    protected Read getById(UriComponentsBuilder uriBuilder) {
        try {
            ResponseEntity<String> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaderWithLanguage()),
                    String.class);

            return renderResponse(response, responseClass);
        } catch (NotFoundException notFound) {
            return null;
        }
    }

    @SneakyThrows
    protected Read create(UriComponentsBuilder uriBuilder, Write write) {
        ResponseEntity<String> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.POST,
                createHttpEntity(write),
                String.class);
        return renderResponse(response, responseClass);
    }

    @SneakyThrows
    protected Read update(UriComponentsBuilder uriBuilder, Write write) {
        ResponseEntity<String> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.PUT,
                createHttpEntity(write),
                String.class);
        return renderResponse(response, responseClass);
    }

    protected void delete(UriComponentsBuilder uriBuilder) {
        ResponseEntity<Void> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.DELETE,
                null, Void.class);
    }

    protected HttpEntity<Write> createHttpEntity(Write write) {
        HttpEntity<Write> entity = new HttpEntity<>(write, createHeaderWithLanguage());
        return entity;
    }

    protected abstract TypeReference<PageableResult<Read>> createPagedTypeReference();

}
