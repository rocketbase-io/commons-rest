package io.rocketbase.commons.config;

import io.rocketbase.commons.posthog.DefaultPostHogWrapper;
import io.rocketbase.commons.posthog.PostHogCaptureApect;
import io.rocketbase.commons.posthog.PostHogUserIdProvider;
import io.rocketbase.commons.posthog.PostHogWrapper;
import io.rocketbase.commons.posthog.client.PostHog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@EnableConfigurationProperties({PosthogProperties.class})
@RequiredArgsConstructor
public class CommonsRestPosthogAutoConfiguration {

    private final PosthogProperties posthogConfig;

    @Bean
    @ConditionalOnMissingBean
    public PostHogWrapper postHogWrapper(@Autowired ApplicationContext applicationContext) {
        PostHogUserIdProvider userIdProvider = getBean(applicationContext, PostHogUserIdProvider.class, null);
        AuditorAware auditorAware = getBean(applicationContext, AuditorAware.class, () -> Optional.empty());

        return new DefaultPostHogWrapper(new PostHog.Builder(posthogConfig.getApikey()).host(posthogConfig.getHost()).build(), userIdProvider, auditorAware);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostHogCaptureApect postHogCaptureApect(@Autowired PostHogWrapper postHogWrapper) {
        return new PostHogCaptureApect(postHogWrapper, posthogConfig);
    }

    protected <T> T getBean(ApplicationContext applicationContext, Class<T> clazz, T fallback) {
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return fallback;
        }
    }
}
