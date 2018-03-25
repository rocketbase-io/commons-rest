package io.rocketbase.commons.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.request.PageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class AbstractRestResource {

    protected final ObjectMapper objectMapper;

    public UriComponentsBuilder appendParams(UriComponentsBuilder uriBuilder, PageableRequest request) {
        if (uriBuilder != null && request != null) {
            if (request.getPage() != null && request.getPage().intValue() >= 0) {
                uriBuilder.queryParam("page", request.getPage());
            }
            if (request.getPageSize() != null && request.getPageSize().intValue() >= 0) {
                uriBuilder.queryParam("pageSize", request.getPageSize());
            }
            if (request.getSort() != null) {
                request.getSort()
                        .iterator()
                        .forEachRemaining(
                                o -> {
                                    uriBuilder.queryParam("sort", String.format("%s,%s", o.getProperty(), o.getDirection().name().toLowerCase()));
                                }
                        );
            }
        }
        return uriBuilder;
    }

    protected <T> T renderResponse(ResponseEntity<String> response, Class<T> responseClass) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), responseClass);
        } else {
            // in case of not found - otherwise it will return empty object
            return null;
        }
    }

    protected <T> T renderResponse(ResponseEntity<String> response, TypeReference<T> typeReference) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), typeReference);
        } else {
            // in case of not found - otherwise it will return empty object
            return null;
        }
    }

    protected HttpHeaders createHeaderWithLanguage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_LANGUAGE,
                LocaleContextHolder.getLocale()
                        .getLanguage());
        return headers;
    }

}
