package io.rocketbase.commons.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
@ConfigurationProperties(prefix = "commons.posthog")
public class PosthogProperties {


    @NotNull
    private String apikey;

    private String host = "https://app.posthog.com";

    private boolean captureWithDuration = true;

}
