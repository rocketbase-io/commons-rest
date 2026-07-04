package io.rocketbase.commons.config;

import io.rocketbase.commons.controller.CodeGeneratorController;
import io.rocketbase.commons.openapi.*;
import lombok.RequiredArgsConstructor;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.List;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties({DataWebProperties.class, OpenApiGeneratorProperties.class})
// low-precedence defaults (e.g. springdoc.api-docs.version=openapi_3_0); overridable by the consuming app
@PropertySource("classpath:commons-rest-openapi-defaults.properties")
public class CommonsRestOpenApiAutoConfiguration {

    private final DataWebProperties springDataWebProperties;
    private final OpenApiGeneratorProperties openApiGeneratorProperties;

    @Bean
    @ConditionalOnMissingBean
    public CodeGeneratorController codeGeneratorController(@Autowired OpenApiClientCreatorService openApiClientCreatorService) {
        return new CodeGeneratorController(openApiGeneratorProperties, openApiClientCreatorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiClientCreatorService openApiClientCreatorService(@Autowired OpenApiWebMvcResource openApiWebMvcResource,
                                                                   @Autowired TypeScriptTypeConverter typeConverter,
                                                                   @Autowired InfiniteOptionsTemplateBuilder templateBuilder,
                                                                   @Autowired(required = false) List<TypeScriptGeneratorCustomizer> typeScriptCustomizers) {
        return new OpenApiClientCreatorService(springDataWebProperties, openApiGeneratorProperties, openApiWebMvcResource, typeConverter, templateBuilder,
                typeScriptCustomizers != null ? typeScriptCustomizers : java.util.Collections.emptyList());
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiCustomExtractor openApiCustomExtractor() {
        return new OpenApiCustomExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeScriptTypeConverter typeScriptTypeConverter() {
        // Provide a default implementation with empty generation result
        // In practice, this will be replaced during actual generation
        return new OpenApiTypeMapper(new TypeScriptGenerationResult("", new HashMap<>()));
    }

    @Bean
    @ConditionalOnMissingBean
    public InfiniteOptionsTemplateBuilder infiniteOptionsTemplateBuilder() {
        return new DefaultInfiniteOptionsTemplateBuilder();
    }


}
