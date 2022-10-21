package io.rocketbase.commons.posthog;

import java.util.Optional;

public interface PostHogUserIdProvider {

    /**
     * identify user by its id (detect with use of security context)
     *
     * @return distinct user id that used for events within posthog-client
     */
    Optional<String> getUserId();
}
