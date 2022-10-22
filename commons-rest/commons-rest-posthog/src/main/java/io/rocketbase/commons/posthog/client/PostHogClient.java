package io.rocketbase.commons.posthog.client;

import java.util.Map;

public interface PostHogClient {
    void capture(String distinctId, String event, Map<String, Object> properties);

    void capture(String distinctId, String event);

    void identify(String distinctId, Map<String, Object> properties, Map<String, Object> propertiesSetOnce);

    void identify(String distinctId, Map<String, Object> properties);

    void alias(String distinctId, String alias);

    void set(String distinctId, Map<String, Object> properties);

    void setOnce(String distinctId, Map<String, Object> properties);
}
