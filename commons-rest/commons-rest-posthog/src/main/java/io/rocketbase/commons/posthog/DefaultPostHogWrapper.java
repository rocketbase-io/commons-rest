package io.rocketbase.commons.posthog;

import io.rocketbase.commons.posthog.client.PostHog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultPostHogWrapper implements PostHogWrapper {

    private final PostHog postHog;

    private final PostHogUserIdProvider userIdProvider;
    private final AuditorAware auditorAware;

    @Override
    public String getUserId() {
        return getUserId(userIdProvider, auditorAware);
    }

    @Override
    public void capture(String event, Map<String, Object> properties) {
        try {
            postHog.capture(getUserId(), event, properties);
        } catch (Exception e) {
            log.warn("capture event: {}, properties: {} - error {}", event, properties, e.getMessage());
        }
    }

    @Override
    public void capture(String event) {
        try {
            postHog.capture(getUserId(), event);
        } catch (Exception e) {
            log.warn("capture event: {} - error {}", event, e.getMessage());
        }
    }

    @Override
    public void identify(Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        identify(getUserId(), properties, propertiesSetOnce);
    }

    @Override
    public void identify(String userId, Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        try {
            postHog.identify(userId, properties, propertiesSetOnce);
        } catch (Exception e) {
            log.warn("identify properties: {}, propertiesSetOnce {} - error {}", properties, propertiesSetOnce, e.getMessage());
        }
    }

    @Override
    public void alias(final String alias) {
        try {
            postHog.alias(getUserId(), alias);
        } catch (Exception e) {
            log.warn("alias alias: {} - error {}", alias, e.getMessage());
        }
    }

    @Override
    public void set(final Map<String, Object> properties) {
        try {
            postHog.set(getUserId(), properties);
        } catch (Exception e) {
            log.warn("set properties: {} - error: {}", properties, e.getMessage());
        }
    }

    @Override
    public void setOnce(final Map<String, Object> properties) {
        try {
            postHog.setOnce(getUserId(), properties);
        } catch (Exception e) {
            log.warn("setOnce properties: {} - error: {}", properties, e.getMessage());
        }
    }

}
