package io.rocketbase.commons.config;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Data
@Validated
@ConfigurationProperties(prefix = "commons.posthog")
public class PostHogProperties {


    @NotNull
    private String apikey;

    private String host = "https://app.posthog.com";

    private boolean captureWithDuration = true;

    @NotNull
    private PostHogClientType clientType = PostHogClientType.HTTP;

    public enum PostHogClientType {
        HTTP,
        LOG;
    }

}
