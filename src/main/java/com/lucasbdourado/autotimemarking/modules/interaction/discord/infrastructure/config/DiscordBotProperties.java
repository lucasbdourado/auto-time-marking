package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord.bot")
public class DiscordBotProperties {

    private boolean enabled = false;
    private String token;
    private String guildId;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    @Override
    public String toString() {
        return "DiscordBotProperties{" +
                "enabled=" + enabled +
                ", token='" + (token != null ? "[PROTECTED]" : "null") + '\'' +
                ", guildId='" + guildId + '\'' +
                '}';
    }
}
