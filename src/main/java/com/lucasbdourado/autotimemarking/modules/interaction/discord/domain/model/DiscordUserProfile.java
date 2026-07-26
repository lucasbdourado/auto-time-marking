package com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model;

import java.util.Objects;

public class DiscordUserProfile {

    private final String discordUserId;
    private String bmaUsername;
    private String bmaPassword;
    private String maxEntryTime = "09:00";
    private Integer jitterMinutes = 5;
    private boolean active = true;

    public DiscordUserProfile(String discordUserId) {
        this.discordUserId = Objects.requireNonNull(discordUserId, "discordUserId must not be null");
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public String getBmaUsername() {
        return bmaUsername;
    }

    public void setBmaUsername(String bmaUsername) {
        this.bmaUsername = bmaUsername;
    }

    public String getBmaPassword() {
        return bmaPassword;
    }

    public void setBmaPassword(String bmaPassword) {
        this.bmaPassword = bmaPassword;
    }

    public String getMaxEntryTime() {
        return maxEntryTime;
    }

    public void setMaxEntryTime(String maxEntryTime) {
        this.maxEntryTime = maxEntryTime;
    }

    public Integer getJitterMinutes() {
        return jitterMinutes;
    }

    public void setJitterMinutes(Integer jitterMinutes) {
        this.jitterMinutes = jitterMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordUserProfile that = (DiscordUserProfile) o;
        return Objects.equals(discordUserId, that.discordUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discordUserId);
    }

    @Override
    public String toString() {
        return "DiscordUserProfile{" +
                "discordUserId='" + discordUserId + '\'' +
                ", bmaUsername='" + bmaUsername + '\'' +
                ", bmaPassword='[PROTECTED]'" +
                ", maxEntryTime='" + maxEntryTime + '\'' +
                ", jitterMinutes=" + jitterMinutes +
                ", active=" + active +
                '}';
    }
}
