package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigurationVerificationHook implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationVerificationHook.class);

    private final BmaquiosqueProperties properties;
    private final BmaquiosquePropertiesValidator validator;

    public ConfigurationVerificationHook(BmaquiosqueProperties properties, BmaquiosquePropertiesValidator validator) {
        this.properties = properties;
        this.validator = validator;
    }

    @Override
    public void afterPropertiesSet() {
        List<String> errors = validator.validate(properties);

        if (errors.isEmpty()) {
            logger.info("Loaded BMAquiosque configuration. User: {}, Max Entry Time: {}, Jitter: {} min, Timezone: {}.",
                    properties.getUsername(),
                    properties.getMaxEntryTime(),
                    properties.getJitterMinutes(),
                    properties.getTimezone());
            return;
        }

        for (String error : errors) {
            logger.error("BMAquiosque configuration error: {}", error);
        }

        throw new IllegalStateException("BMAquiosque configuration validation failed");
    }
}
