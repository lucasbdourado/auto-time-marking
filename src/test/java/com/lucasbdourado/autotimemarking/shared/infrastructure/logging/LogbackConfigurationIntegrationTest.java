package com.lucasbdourado.autotimemarking.shared.infrastructure.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogbackConfigurationIntegrationTest {

    @Test
    void shouldStartLogbackContextWithoutErrors() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfiguration.class)
                .web(WebApplicationType.NONE)
                .run()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            List<Status> errors = loggerContext.getStatusManager()
                    .getCopyOfStatusList()
                    .stream()
                    .filter(status -> status.getLevel() == Status.ERROR)
                    .toList();

            assertThat(context.isActive()).isTrue();
            assertThat(loggerContext.isStarted()).isTrue();
            assertThat(errors).isEmpty();
        }
    }

    @SpringBootConfiguration
    static class TestConfiguration {
    }
}
