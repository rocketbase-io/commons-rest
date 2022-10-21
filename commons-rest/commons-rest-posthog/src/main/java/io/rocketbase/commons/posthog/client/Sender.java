package io.rocketbase.commons.posthog.client;


import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.List;

public interface Sender {
    public void send(List<JSONObject> events);
}
