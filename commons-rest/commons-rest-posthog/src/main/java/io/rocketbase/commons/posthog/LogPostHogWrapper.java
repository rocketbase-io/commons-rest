package io.rocketbase.commons.posthog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Map;


/**
 * could be activated within tests
 */
@Slf4j
@RequiredArgsConstructor
public class LogPostHogWrapper implements PostHogWrapper {

    private final PostHogUserIdProvider userIdProvider;
    private final AuditorAware auditorAware;

    @Override
    public String getUserId() {
        return getUserId(userIdProvider, auditorAware);
    }

    @Override
    public void capture(String event, Map<String, Object> properties) {
        log.info("{} - capture: {}, {}", getUserId(), event, properties);
    }

    @Override
    public void capture(String event) {
        log.info("{} - capture: {}", getUserId(), event);
    }

    @Override
    public void identify(Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        identify(getUserId(), properties, propertiesSetOnce);
    }

    @Override
    public void identify(String userId, Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        log.info("{} - identify: {}, {}", userId, properties, propertiesSetOnce);
    }

    @Override
    public void alias(String alias) {
        log.info("{} - alias: {}", getUserId(), alias);
    }

    @Override
    public void set(Map<String, Object> properties) {
        log.info("{} - set: {}", getUserId(), properties);
    }

    @Override
    public void setOnce(Map<String, Object> properties) {
        log.info("{} - setOnce: {}", getUserId(), properties);
    }
}
