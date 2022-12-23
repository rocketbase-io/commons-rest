package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.commons.resource.BaseRestResource;
import io.rocketbase.commons.resource.BasicResponseErrorHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

public abstract class AbstractBaseCrudRestResource<Read, Write> implements BaseRestResource {

    @Getter
    protected Class<Read> responseClass;

    @Setter
    private RestTemplate restTemplate;

    public AbstractBaseCrudRestResource() {
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
        ResponseEntity<PageableResult<Read>> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(createHeaderWithLanguage()),
                createPagedTypeReference());
        return response.getBody();
    }

    /**
     * @param uriBuilder complete uri
     * @return optional in case of {@link io.rocketbase.commons.exception.NotFoundException}) empty
     */
    @SneakyThrows
    protected Optional<Read> getById(UriComponentsBuilder uriBuilder) {
        try {
            ResponseEntity<Read> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaderWithLanguage()),
                    responseClass);

            return Optional.of(response.getBody());
        } catch (NotFoundException notFound) {
            return Optional.empty();
        }
    }

    @SneakyThrows
    protected Read create(UriComponentsBuilder uriBuilder, Write write) {
        ResponseEntity<Read> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.POST,
                createHttpEntity(write),
                responseClass);
        return response.getBody();
    }

    @SneakyThrows
    protected Read update(UriComponentsBuilder uriBuilder, Write write) {
        ResponseEntity<Read> response = getRestTemplate().exchange(uriBuilder.toUriString(),
                HttpMethod.PUT,
                createHttpEntity(write),
                responseClass);
        return response.getBody();
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

    protected abstract ParameterizedTypeReference<PageableResult<Read>> createPagedTypeReference();

}
