package io.rocketbase.commons.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "commons.logging")
public class LoggingAspectProperties {

    private boolean trim = true;
    private boolean duration = true;
    private boolean skipArgs = true;
    private boolean skipResult = true;
    private boolean query = true;
    private String logLevel = "DEBUG";

    /**
     * NONE will disable error logs
     */
    private String errorLogLevel = "WARN";
    private int trimLength = 100;

}
