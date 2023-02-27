package io.rocketbase.commons.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "commons.logging")
public class LoggingAspectProperties {

    private boolean trim = true;
    private boolean duration = true;
    private boolean audit = true;
    private boolean args = false;
    private boolean result = false;
    private boolean query = true;

    @NotNull
    private String logLevel = "DEBUG";

    /**
     * NONE will disable error logs
     */
    @NotNull
    private String errorLogLevel = "WARN";
    private int trimLength = 100;

}
