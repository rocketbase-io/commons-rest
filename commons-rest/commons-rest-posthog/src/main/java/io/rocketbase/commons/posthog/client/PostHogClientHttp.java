package io.rocketbase.commons.posthog.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PostHogClientHttp implements PostHogClient {
    private QueueManager queueManager;
    private Thread queueManagerThread;

    private static abstract class BuilderBase {
        protected QueueManager queueManager;
    }

    public static class Builder extends BuilderBase {
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

        public PostHogClient build() {
            HttpSender sender = new HttpSender.Builder(apiKey).host(host).build();
            this.queueManager = new QueueManager.Builder(sender).build();
            return new PostHogClientHttp(this);
        }
    }

    public static class BuilderWithCustomQueueManager extends BuilderBase {

        public BuilderWithCustomQueueManager(QueueManager queueManager) {
            this.queueManager = queueManager;
        }

        public PostHogClient build() {
            return new PostHogClientHttp(this);
        }
    }

    private PostHogClientHttp(BuilderBase builder) {
        this.queueManager = builder.queueManager;
        startQueueManager();
    }

    public void shutdown() {
        queueManager.stop();
        try {
            queueManagerThread.join(); // wait for the current items in queue to be sent
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            log.warn("shutdown: {}", e.getMessage());
        }
    }

    private void startQueueManager() {
        queueManagerThread = new Thread(queueManager, "PostHog QueueManager thread");
        queueManagerThread.start();
        // TODO handle interrupts? (via addShutdownHook)
    }

    private void enqueue(String distinctId, String event, Map<String, Object> properties) {
        JSONObject eventJson = getEventJson(event, distinctId, properties);
        queueManager.add(eventJson);
    }

    /**
     * @param distinctId which uniquely identifies your user in your database. Must
     *                   not be null or empty.
     * @param event      name of the event. Must not be null or empty.
     * @param properties an array with any event properties you'd like to set.
     */
    @Override
    public void capture(String distinctId, String event, Map<String, Object> properties) {
        enqueue(distinctId, event, properties);
    }

    /**
     * @param distinctId which uniquely identifies your user in your database. Must
     *                   not be null or empty.
     * @param event      name of the event. Must not be null or empty.
     */
    @Override
    public void capture(String distinctId, String event) {
        enqueue(distinctId, event, null);
    }

    /**
     * @param distinctId        which uniquely identifies your user in your
     *                          database. Must not be null or empty.
     * @param properties        an array with any person properties you'd like to
     *                          set.
     * @param propertiesSetOnce an array with any person properties you'd like to
     *                          set without overwriting previous values.
     */
    @Override
    public void identify(String distinctId, Map<String, Object> properties, Map<String, Object> propertiesSetOnce) {
        Map<String, Object> props = new HashMap<String, Object>();
        if (properties != null) {
            props.put("$set", properties);
        }
        if (propertiesSetOnce != null) {
            props.put("$set_once", propertiesSetOnce);
        }
        enqueue(distinctId, "$identify", props);
    }

    /**
     * @param distinctId which uniquely identifies your user in your database. Must
     *                   not be null or empty.
     * @param properties an array with any person properties you'd like to set.
     */
    @Override
    public void identify(String distinctId, Map<String, Object> properties) {
        identify(distinctId, properties, null);
    }

    /**
     * @param distinctId distinct ID to merge. Must not be null or empty. Note: If
     *                   there is a conflict, the properties of this person will
     *                   take precedence.
     * @param alias      distinct ID to merge. Must not be null or empty. Note: If
     *                   there is a conflict, the properties of this person will be
     *                   overriden.
     */
    @Override
    public void alias(String distinctId, String alias) {
        Map<String, Object> props = new HashMap<String, Object>() {
            {
                put("distinct_id", distinctId);
                put("alias", alias);
            }
        };
        enqueue(distinctId, "$create_alias", props);
    }

    /**
     * @param distinctId which uniquely identifies your user in your database. Must
     *                   not be null or empty.
     * @param properties an array with any person properties you'd like to set.
     */
    @Override
    public void set(String distinctId, Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<String, Object>() {
            {
                put("$set", properties);
            }
        };
        enqueue(distinctId, "$set", props);
    }

    /**
     * @param distinctId which uniquely identifies your user in your database. Must
     *                   not be null or empty.
     * @param properties an array with any person properties you'd like to set.
     *                   Previous values will not be overwritten.
     */
    @Override
    public void setOnce(String distinctId, Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<String, Object>() {
            {
                put("$set_once", properties);
            }
        };
        enqueue(distinctId, "$set_once", props);
    }

    private JSONObject getEventJson(String event, String distinctId, Map<String, Object> properties) {
        JSONObject eventJson = new JSONObject();
        try {
            eventJson.put("timestamp", Instant.now().toString());
            eventJson.put("distinct_id", distinctId);
            eventJson.put("event", event);
            if (properties != null) {
                eventJson.put("properties", properties);
            }
        } catch (JSONException e) {
            log.warn("getEventJson: {}", e.getMessage());
        }
        return eventJson;
    }
}
