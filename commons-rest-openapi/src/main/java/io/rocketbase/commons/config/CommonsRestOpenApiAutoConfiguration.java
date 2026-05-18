package io.rocketbase.commons.config;

import io.rocketbase.commons.controller.CodeGeneratorController;
import io.rocketbase.commons.openapi.*;
import lombok.RequiredArgsConstructor;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;

@Configuration
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties({SpringDataWebProperties.class, OpenApiGeneratorProperties.class})
public class CommonsRestOpenApiAutoConfiguration {

    private final SpringDataWebProperties springDataWebProperties;
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
