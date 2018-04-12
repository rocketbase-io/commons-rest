package io.rocketbase.commons.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class AbstractRestResource implements BaseRestResource {

    protected final ObjectMapper objectMapper;

    protected <T> T renderResponse(ResponseEntity<String> response, Class<T> responseClass) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), responseClass);
        } else {
            // in case of no error has thrown but the status is not 2xx
            return null;
        }
    }

    protected <T> T renderResponse(ResponseEntity<String> response, TypeReference<T> typeReference) throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), typeReference);
        } else {
            // in case of no error has thrown but the status is not 2xx
            return null;
        }
    }

}
