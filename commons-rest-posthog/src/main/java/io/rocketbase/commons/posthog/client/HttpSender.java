package io.rocketbase.commons.posthog.client;


import io.rocketbase.commons.util.UrlParts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class HttpSender {
    private String apiKey;
    private String host;
    private RestTemplate restTemplate;

    private static HttpHeaders httpHeaders;

    static {
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
    }


    public static class Builder {
        // required
        private final String apiKey;

        // optional
        private String host = "https://app.posthog.com";

        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public HttpSender build() {
            return new HttpSender(this);
        }
    }

    private HttpSender(Builder builder) {
        this.apiKey = builder.apiKey;
        this.host = UrlParts.ensureEndsWithSlash(builder.host);
        this.restTemplate = new RestTemplate();
    }

    public void send(List<JSONObject> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        try {
            restTemplate.exchange(host + "batch", HttpMethod.POST, new HttpEntity<>(getRequestBody(events), httpHeaders), Void.class);
        } catch (Exception e) {
            log.warn("send: {}", e.getMessage());
        }
    }

    private String getRequestBody(List<JSONObject> events) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api_key", apiKey);
            jsonObject.put("batch", events);
        } catch (JSONException e) {
            log.warn("getRequestBody: {}", e.getMessage());
        }
        return jsonObject.toString();
    }

}
