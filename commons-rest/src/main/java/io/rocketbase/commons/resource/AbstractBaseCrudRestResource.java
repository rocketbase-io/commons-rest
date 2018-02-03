package io.rocketbase.commons.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractBaseCrudRestResource<ResponseData, EditData> extends AbstractRestResource {

    protected RestTemplate restTemplate;

    @Getter
    protected Class<ResponseData> responseClass;

    @Autowired
    public AbstractBaseCrudRestResource(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(objectMapper);
        restTemplate.setErrorHandler(new NoopResponseErrorHandler());
        this.restTemplate = restTemplate;
        responseClass = (Class<ResponseData>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }


    @SneakyThrows
    protected PageableResult<ResponseData> find(UriComponentsBuilder uriBuilder) {
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(createHeaderWithLanguage()),
                String.class);
        return renderResponse(response, createPagedTypeReference());
    }

    @SneakyThrows
    protected ResponseData getById(UriComponentsBuilder uriBuilder) {
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(createHeaderWithLanguage()),
                String.class);
        return renderResponse(response, responseClass);
    }

    @SneakyThrows
    protected ResponseData create(UriComponentsBuilder uriBuilder, EditData editData) {
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.POST,
                createHttpEntity(editData),
                String.class);
        return renderResponse(response, responseClass);
    }

    @SneakyThrows
    protected ResponseData update(UriComponentsBuilder uriBuilder, EditData editData) {
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.PUT,
                createHttpEntity(editData),
                String.class);
        return renderResponse(response, responseClass);
    }

    protected void delete(UriComponentsBuilder uriBuilder) {
        ResponseEntity<Void> response = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.DELETE,
                null, Void.class);
    }

    protected HttpEntity<EditData> createHttpEntity(EditData editData) {
        HttpEntity<EditData> entity = new HttpEntity<>(editData, createHeaderWithLanguage());
        return entity;
    }

    protected abstract TypeReference<PageableResult<ResponseData>> createPagedTypeReference();

}
