package io.rocketbase.sample.config;

import io.rocketbase.sample.BaseIntegrationTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
public class ConfigTest extends BaseIntegrationTest {

    @Resource
    private LocaleResolver localeResolver;

    @Test
    public void shouldGetSupportedLocales() throws Exception {
        // configuration is set in project:
        // locale.resolver:
        //     default: de
        //     supported: de, EN_us
        assertThat(localeResolver, instanceOf(AcceptHeaderLocaleResolver.class));
        assertThat(((AcceptHeaderLocaleResolver) localeResolver).getSupportedLocales(), containsInAnyOrder(Locale.GERMAN, Locale.US));
        assertThat(((AcceptHeaderLocaleResolver) localeResolver).getSupportedLocales().size(), equalTo(2));
    }

}
