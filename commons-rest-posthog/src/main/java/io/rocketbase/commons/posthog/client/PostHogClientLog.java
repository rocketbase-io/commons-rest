package io.rocketbase.commons.posthog.client;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PostHogClientLog implements PostHogClient {
    @Override
    public void capture(String distinctId, String event, Map<String, Object> properties) {
        log.info("capture: {}, {}, {}", distinctId, event, properties);
    }

    @Override
    public void capture(String distinctId, String event) {
        log.info("capture: {}, {}", distinctId, event);
    }

    @Override
    public void identify(String distinctId, Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        log.info("identify: {}, {}, {}", distinctId, properties, propertiesSetOnce);
    }

    @Override
    public void identify(String distinctId, Map<String, Object> properties) {
        log.info("identify: {}, {}", distinctId, properties);
    }

    @Override
    public void alias(String distinctId, String alias) {
        log.info("alias: {}, {}", distinctId, alias);
    }

    @Override
    public void set(String distinctId, Map<String, Object> properties) {
        log.info("set: {}, {}", distinctId, properties);
    }

    @Override
    public void setOnce(String distinctId, Map<String, Object> properties) {
        log.info("setOnce: {}, {}", distinctId, properties);
    }
}
