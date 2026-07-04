package io.rocketbase.commons.resource;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.exception.InsufficientPrivilegesException;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class BasicResponseErrorHandler extends DefaultResponseErrorHandler {

    private ObjectMapper objectMapper;

    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = JsonMapper.builder()
                    .findAndAddModules()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .build();
        }
        return objectMapper;
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().equals(BAD_REQUEST)) {
            throw new BadRequestException(readErrorResponse(response, BAD_REQUEST));
        } else if (response.getStatusCode().equals(NOT_FOUND)) {
            throw new NotFoundException(readErrorResponse(response, NOT_FOUND));
        } else if (response.getStatusCode().equals(FORBIDDEN)) {
            throw new InsufficientPrivilegesException(readErrorResponse(response, FORBIDDEN));
        } else {
            super.handleError(url, method, response);
        }
    }

    protected ErrorResponse readErrorResponse(ClientHttpResponse response, HttpStatus fallback) {
        try {
            return getObjectMapper().readValue(response.getBody(), ErrorResponse.class);
        } catch (Exception e) {
            return new ErrorResponse(fallback.value(), fallback.getReasonPhrase());
        }
    }

}
