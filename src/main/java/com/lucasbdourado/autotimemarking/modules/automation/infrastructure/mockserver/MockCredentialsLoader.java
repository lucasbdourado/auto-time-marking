package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.mockserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MockCredentialsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockCredentialsLoader.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String validUsername = "365";
    private String validPassword = "LucKing@15973";

    @PostConstruct
    public void loadCredentials() {
        try {
            ClassPathResource resource = new ClassPathResource("credentials.json");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    JsonNode node = objectMapper.readTree(inputStream);
                    if (node.has("username")) {
                        this.validUsername = node.get("username").asText();
                    }
                    if (node.has("password")) {
                        this.validPassword = node.get("password").asText();
                    }
                    LOGGER.info("Loaded mock credentials for username: {}", this.validUsername);
                }
            } else {
                LOGGER.warn("credentials.json not found on classpath, using defaults: username={}", validUsername);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read credentials.json, falling back to default credentials", e);
        }
    }

    public boolean isValidUser(String username, String password) {
        if ("pass.test".equalsIgnoreCase(password)) {
            return true;
        }
        return this.validUsername.equals(username) && this.validPassword.equals(password);
    }

    public boolean isValidPassword(String password) {
        if ("pass.test".equalsIgnoreCase(password)) {
            return true;
        }
        return this.validPassword.equals(password);
    }

    public String getValidUsername() {
        return validUsername;
    }

    public String getValidPassword() {
        return validPassword;
    }
}
