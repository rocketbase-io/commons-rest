package io.rocketbase.commons.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class AbstractRestResource {

    protected final ObjectMapper objectMapper;

    protected <T> T renderResponse(ResponseEntity<String> response, Class<T> responseClass) throws IOException {
        if (response.getStatusCode()
                .is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), responseClass);
        } else {
            handleErrorStatus(response);
        }
        return null;
    }

    protected <T> T renderResponse(ResponseEntity<String> response, TypeReference<T> typeReference) throws IOException {
        if (response.getStatusCode()
                .is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), typeReference);
        } else {
            handleErrorStatus(response);
        }
        return null;
    }

    protected HttpHeaders createHeaderWithLanguage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_LANGUAGE,
                LocaleContextHolder.getLocale()
                        .getLanguage());
        return headers;
    }

    protected void handleErrorStatus(ResponseEntity<String> response) throws IOException {
        if (response.getStatusCode()
                .equals(HttpStatus.BAD_REQUEST)) {
            ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
            throw new BadRequestException(errorResponse);
        } else if (response.getStatusCode()
                .is5xxServerError()) {
            throw new InternalServerErrorException(response.getBody());
        }
    }

}
