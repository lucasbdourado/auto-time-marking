package com.lucasbdourado.autotimemarking.modules.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord.notification")
public class NotificationProperties {

    private boolean enabled = true;
    private String defaultChannelId;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultChannelId() {
        return defaultChannelId;
    }

    public void setDefaultChannelId(String defaultChannelId) {
        this.defaultChannelId = defaultChannelId;
    }
}
