package io.rocketbase.commons.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.data.web")
public class OpenApiGeneratorProperties {

    private String baseUrl = "/api";
    private String groupName = "ModuleApi";
    private String modelFolder = "model";
    private String hookFolder = "hooks";
    private String clientFolder = "clients";
}
