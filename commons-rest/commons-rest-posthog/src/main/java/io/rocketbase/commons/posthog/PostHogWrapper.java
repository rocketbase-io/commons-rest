package io.rocketbase.commons.posthog;

import org.springframework.data.domain.AuditorAware;

import java.util.Map;

public interface PostHogWrapper {

    String getUserId();


    default String getUserId(PostHogUserIdProvider userIdProvider, AuditorAware auditorAware) {
        if (userIdProvider != null && userIdProvider.getUserId().isPresent()) {
            return userIdProvider.getUserId().get();
        }
        if (auditorAware != null && auditorAware.getCurrentAuditor().isPresent()) {
            return String.valueOf(auditorAware.getCurrentAuditor().get());
        }
        return "unknown";
    }

    void capture(String event, Map<String, Object> properties);

    void capture(String event);

    /**
     * uses getUserId() so that only the current loggedin user will get identified
     */
    void identify(Map<String, Object> properties, Map<String, Object> setOnce);

    /**
     * could be used to sync multiple users within batch
     */
    void identify(String userId, Map<String, Object> properties, Map<String, Object> setOnce);

    void alias(String alias);

    void set(Map<String, Object> properties);

    void setOnce(Map<String, Object> properties);
}
