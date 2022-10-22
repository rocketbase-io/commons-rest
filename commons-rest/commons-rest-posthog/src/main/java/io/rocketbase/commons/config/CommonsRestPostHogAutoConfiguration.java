package io.rocketbase.commons.config;

import io.rocketbase.commons.posthog.DefaultPostHogService;
import io.rocketbase.commons.posthog.PostHogCaptureApect;
import io.rocketbase.commons.posthog.PostHogService;
import io.rocketbase.commons.posthog.PostHogUserIdProvider;
import io.rocketbase.commons.posthog.client.PostHogClient;
import io.rocketbase.commons.posthog.client.PostHogClientHttp;
import io.rocketbase.commons.posthog.client.PostHogClientLog;
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

import static io.rocketbase.commons.config.PostHogProperties.PostHogClientType.HTTP;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({PostHogProperties.class})
@RequiredArgsConstructor
public class CommonsRestPostHogAutoConfiguration {

    private final PostHogProperties posthogConfig;

    @Bean
    @ConditionalOnMissingBean
    public PostHogService postHogService(@Autowired ApplicationContext applicationContext) {
        PostHogUserIdProvider userIdProvider = getBean(applicationContext, PostHogUserIdProvider.class, null);
        AuditorAware auditorAware = getBean(applicationContext, AuditorAware.class, () -> Optional.empty());

        PostHogClient postHogClient = HTTP.equals(posthogConfig.getClientType()) ?
                new PostHogClientHttp.Builder(posthogConfig.getApikey()).host(posthogConfig.getHost()).build() :
                new PostHogClientLog();
        return new DefaultPostHogService(postHogClient, userIdProvider, auditorAware);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostHogCaptureApect postHogCaptureApect(@Autowired PostHogService postHogService) {
        return new PostHogCaptureApect(postHogService, posthogConfig);
    }

    protected <T> T getBean(ApplicationContext applicationContext, Class<T> clazz, T fallback) {
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return fallback;
        }
    }
}
