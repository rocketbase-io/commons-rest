package io.rocketbase.commons.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.ParameterizedType;


@Slf4j
public abstract class BaseCrudRestResource<ResponseData, EditData> extends BaseRestResource {

    protected RestTemplate restTemplate;

    protected String baseDomain = "";

    @Getter
    private Class<ResponseData> responseClass;

    @Autowired
    public BaseCrudRestResource(RestTemplate restTemplate, String baseDomain, ObjectMapper objectMapper) {
        super(objectMapper);
        restTemplate.setErrorHandler(new NoopResponseErrorHandler());
        this.restTemplate = restTemplate;
        this.baseDomain = baseDomain;
        responseClass = (Class<ResponseData>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public PageableResult<ResponseData> find(int page, int pagesize) {
        return find(UriComponentsBuilder.fromUriString(baseDomain + getBaseApiUrl())
                .queryParam("page", page)
                .queryParam("pageSize", pagesize));
    }

    @SneakyThrows
    protected PageableResult<ResponseData> find(UriComponentsBuilder uriBuilder) {
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(createHeaderWithLanguage()),
                String.class);
        return renderResponse(response, createPagedTypeReference());
    }

    public ResponseData getById(String id) {
        return getById(UriComponentsBuilder.fromUriString(baseDomain +
                getBaseApiUrl() + "/" + id));
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
    public ResponseData create(EditData editData) {
        ResponseEntity<String> response = restTemplate.exchange(baseDomain +
                        getBaseApiUrl(),
                HttpMethod.POST,
                createHttpEntity(editData),
                String.class);
        return renderResponse(response, responseClass);
    }

    @SneakyThrows
    public ResponseData update(String id, EditData editData) {
        ResponseEntity<String> response = restTemplate.exchange(baseDomain +
                        getBaseApiUrl() + "/{id}",
                HttpMethod.PUT,
                createHttpEntity(editData),
                String.class,
                id);
        return renderResponse(response, responseClass);
    }

    public void delete(String id) {
        ResponseEntity<Void> response = restTemplate.exchange(baseDomain +
                        getBaseApiUrl() + "/" + id,
                HttpMethod.DELETE,
                null, Void.class);
    }

    protected HttpEntity<EditData> createHttpEntity(EditData editData) {
        HttpEntity<EditData> entity = new HttpEntity<>(editData, createHeaderWithLanguage());
        return entity;
    }

    protected abstract String getBaseApiUrl();

    protected abstract TypeReference<PageableResult<ResponseData>> createPagedTypeReference();

}
