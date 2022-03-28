package io.rocketbase.commons.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties("commons.openapi.generator")
public class OpenApiGeneratorProperties {

    private String baseUrl = "/api";
    private String groupName = "ModuleApi";
    private String hookFolder = "hooks";
    private String clientFolder = "clients";

    private String modelFolder = "model";
    private boolean modelCreate = true;
    private List<String> modelImports = new ArrayList<>();
}
