package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationIntegrationTest {

    @Test
    void shouldStartApplicationContextWhenConfigurationIsValid() {
        try (ConfigurableApplicationContext context = runContext(
                "bmaquiosque.username=user",
                "bmaquiosque.password=password",
                "bmaquiosque.max-entry-time=09:00",
                "bmaquiosque.jitter-minutes=5",
                "bmaquiosque.timezone=America/Sao_Paulo")) {
            assertThat(context.isActive()).isTrue();
        }
    }

    @Test
    void shouldFailApplicationContextStartupWhenMaxEntryTimeIsInvalid() {
        assertThatThrownBy(() -> runContext(
                "bmaquiosque.username=user",
                "bmaquiosque.password=password",
                "bmaquiosque.max-entry-time=04:59",
                "bmaquiosque.jitter-minutes=5",
                "bmaquiosque.timezone=America/Sao_Paulo"))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("BMAquiosque configuration validation failed");
    }

    @Test
    void shouldFailApplicationContextStartupWhenTimezoneIsInvalid() {
        assertThatThrownBy(() -> runContext(
                "bmaquiosque.username=user",
                "bmaquiosque.password=password",
                "bmaquiosque.max-entry-time=09:00",
                "bmaquiosque.jitter-minutes=5",
                "bmaquiosque.timezone=Invalid/Timezone"))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("BMAquiosque configuration validation failed");
    }

    private ConfigurableApplicationContext runContext(String... properties) {
        return new SpringApplicationBuilder(TestConfiguration.class)
                .web(WebApplicationType.NONE)
                .run(toCommandLineArgs(properties));
    }

    private String[] toCommandLineArgs(String... properties) {
        return Arrays.stream(properties)
                .map(property -> "--" + property)
                .toArray(String[]::new);
    }

    @SpringBootConfiguration
    static class TestConfiguration {

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        BmaquiosquePropertiesValidator bmaquiosquePropertiesValidator(Validator validator) {
            return new BmaquiosquePropertiesValidator(validator);
        }

        @Bean
        BmaquiosqueProperties bmaquiosqueProperties(Environment environment) {
            BmaquiosqueProperties properties = new BmaquiosqueProperties();
            properties.setUsername(environment.getProperty("bmaquiosque.username"));
            properties.setPassword(environment.getProperty("bmaquiosque.password"));
            properties.setMaxEntryTime(environment.getProperty("bmaquiosque.max-entry-time"));
            properties.setJitterMinutes(environment.getProperty("bmaquiosque.jitter-minutes", Integer.class));
            properties.setTimezone(environment.getProperty("bmaquiosque.timezone"));
            properties.setUrl(environment.getProperty("bmaquiosque.url", "https://bmaquiosque.example.com"));

            BmaquiosqueProperties.Selectors selectors = new BmaquiosqueProperties.Selectors();
            selectors.setUsername(environment.getProperty("bmaquiosque.selectors.username", "input[name='username']"));
            selectors.setPassword(environment.getProperty("bmaquiosque.selectors.password", "input[name='password']"));
            selectors.setLoginButton(environment.getProperty("bmaquiosque.selectors.login-button", "button[type='submit']"));
            selectors.setMarkingsContainer(environment.getProperty("bmaquiosque.selectors.markings-container", ".marking-time-text"));
            selectors.setPunchButton(environment.getProperty("bmaquiosque.selectors.punch-button", "#btn-punch"));
            properties.setSelectors(selectors);

            return properties;
        }

        @Bean
        ConfigurationVerificationHook configurationVerificationHook(
                BmaquiosqueProperties properties,
                BmaquiosquePropertiesValidator validator
        ) {
            return new ConfigurationVerificationHook(properties, validator);
        }
    }
}
