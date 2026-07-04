package io.rocketbase.commons.resource;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
            ErrorResponse errorResponse = null;
            try {
                errorResponse = getObjectMapper().readValue(response.getBody(), ErrorResponse.class);
            } catch (Exception e) {
                errorResponse = new ErrorResponse(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase());
            }
            throw new BadRequestException(errorResponse);
        } else if (response.getStatusCode().equals(NOT_FOUND)) {
            ErrorResponse errorResponse = null;
            try {
                errorResponse = getObjectMapper().readValue(response.getBody(), ErrorResponse.class);
            } catch (Exception e) {
                errorResponse = new ErrorResponse(NOT_FOUND.value(), NOT_FOUND.getReasonPhrase());
            }
            throw new NotFoundException(errorResponse);
        } else {
            super.handleError(url, method, response);
        }
    }

}
