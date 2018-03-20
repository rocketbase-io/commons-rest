package io.rocketbase.commons.resource;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class NoopResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return !response.getStatusCode()
                .is2xxSuccessful();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
    }
}
