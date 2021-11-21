package io.rocketbase.commons.config;

import io.rocketbase.commons.controller.CodeGeneratorController;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class CommonsRestAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CodeGeneratorController codeGeneratorController() {
        return new CodeGeneratorController();
    }


}
