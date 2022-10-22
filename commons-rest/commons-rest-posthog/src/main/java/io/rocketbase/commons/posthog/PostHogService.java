package io.rocketbase.commons.posthog;

import org.springframework.data.domain.AuditorAware;

import java.util.Map;

public interface PostHogService {

    /**
     * distinct userId is predefined so that actions don't need to get added userId
     */
    interface PostHogCurrent {
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

        void identify(Map<String, Object> properties, Map<String, Object> setOnce);

        void alias(String alias);

        void set(Map<String, Object> properties);

        void setOnce(Map<String, Object> properties);
    }

    /**
     * predefined distinctId by PostHogUserIdProvider / security context
     */
    PostHogCurrent current();


    void capture(String distinctId, String event, Map<String, Object> properties);

    void capture(String distinctId, String event);

    void alias(String distinctId, String alias);

    void set(String distinctId, Map<String, Object> properties);

    void setOnce(String distinctId, Map<String, Object> properties);

    void identify(String distinctId, Map<String, Object> properties, Map<String, Object> setOnce);


}
