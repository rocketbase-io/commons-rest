package io.rocketbase.commons.config;

import io.rocketbase.commons.logging.LoggableConfig;
import io.rocketbase.commons.logging.MethodLogger;
import io.rocketbase.commons.logging.RequestLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({LoggingAspectProperties.class})
@RequiredArgsConstructor
public class CommonsRestLoggingAutoConfiguration {

    private final LoggingAspectProperties logAspectConfig;

    private AuditorAware auditorAware;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "commons.logging.mvc.enabled", matchIfMissing = true)
    public RequestLogger requestLogger(@Autowired ApplicationContext applicationContext) {
        return new RequestLogger(getAuditorAware(applicationContext), new LoggableConfig(logAspectConfig));
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodLogger methodLogger(@Autowired ApplicationContext applicationContext) {
        return new MethodLogger(getAuditorAware(applicationContext));
    }

    protected AuditorAware getAuditorAware(ApplicationContext applicationContext) {
        if (auditorAware == null) {
            try {
                auditorAware = applicationContext.getBean(AuditorAware.class);
            } catch (BeansException e) {
                log.debug("logging-aspect: in order to add audit configure AuditorAware bean!");
                auditorAware = () -> Optional.empty();
            }
        }
        return auditorAware;
    }

}
