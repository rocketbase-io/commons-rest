package io.rocketbase.commons.posthog;

import io.rocketbase.commons.posthog.client.PostHogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Map;

@Slf4j
public class DefaultPostHogService implements PostHogService {

    private final PostHogClient postHogClient;
    private final DefaultPostHogCurrent current;

    public DefaultPostHogService(PostHogClient postHogClient, PostHogUserIdProvider userIdProvider, AuditorAware auditorAware) {
        this.postHogClient = postHogClient;
        this.current = new DefaultPostHogCurrent(userIdProvider, auditorAware, this);
    }

    @RequiredArgsConstructor
    public static class DefaultPostHogCurrent implements PostHogCurrent {

        private final PostHogUserIdProvider userIdProvider;
        private final AuditorAware auditorAware;

        private final DefaultPostHogService postHogWrapper;

        @Override
        public String getUserId() {
            return getUserId(userIdProvider, auditorAware);
        }

        @Override
        public void capture(String event, Map<String, Object> properties) {
            postHogWrapper.capture(getUserId(), event, properties);
        }

        @Override
        public void capture(String event) {
            postHogWrapper.capture(getUserId(), event);
        }

        @Override
        public void identify(Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
            postHogWrapper.identify(getUserId(), properties, propertiesSetOnce);
        }

        @Override
        public void alias(final String alias) {
            postHogWrapper.alias(getUserId(), alias);
        }

        @Override
        public void set(final Map<String, Object> properties) {
            postHogWrapper.set(getUserId(), properties);
        }

        @Override
        public void setOnce(final Map<String, Object> properties) {
            postHogWrapper.setOnce(getUserId(), properties);
        }

    }

    @Override
    public PostHogCurrent current() {
        return current;
    }

    @Override
    public void capture(String distinctId, String event, Map<String, Object> properties) {
        try {
            postHogClient.capture(distinctId, event, properties);
        } catch (Exception e) {
            log.warn("capture event: {}, properties: {} - error {}", event, properties, e.getMessage());
        }
    }

    @Override
    public void capture(String distinctId, String event) {
        try {
            postHogClient.capture(distinctId, event);
        } catch (Exception e) {
            log.warn("capture event: {} - error {}", event, e.getMessage());
        }
    }

    @Override
    public void alias(String distinctId, String alias) {
        try {
            postHogClient.alias(distinctId, alias);
        } catch (Exception e) {
            log.warn("alias alias: {} - error {}", alias, e.getMessage());
        }
    }

    @Override
    public void set(String distinctId, Map<String, Object> properties) {
        try {
            postHogClient.set(distinctId, properties);
        } catch (Exception e) {
            log.warn("set properties: {} - error: {}", properties, e.getMessage());
        }
    }

    @Override
    public void setOnce(String distinctId, Map<String, Object> properties) {
        try {
            postHogClient.setOnce(distinctId, properties);
        } catch (Exception e) {
            log.warn("setOnce properties: {} - error: {}", properties, e.getMessage());
        }
    }

    @Override
    public void identify(String userId, Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        try {
            postHogClient.identify(userId, properties, propertiesSetOnce);
        } catch (Exception e) {
            log.warn("identify properties: {}, propertiesSetOnce {} - error {}", properties, propertiesSetOnce, e.getMessage());
        }
    }

}
