package io.rocketbase.commons.resource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class BasicResponseErrorHandler extends DefaultResponseErrorHandler {

    private ObjectMapper objectMapper;

    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper().findAndRegisterModules();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        return objectMapper;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().equals(BAD_REQUEST)) {
            ErrorResponse errorResponse = null;
            try {
                errorResponse = getObjectMapper().readValue(response.getBody(), ErrorResponse.class);
            } catch (Exception e) {
                errorResponse = new ErrorResponse(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase());
            }
            throw new BadRequestException(errorResponse);
        } else if (response.getStatusCode().equals(NOT_FOUND)) {
            throw new NotFoundException();
        } else {
            super.handleError(response);
        }
    }

}
