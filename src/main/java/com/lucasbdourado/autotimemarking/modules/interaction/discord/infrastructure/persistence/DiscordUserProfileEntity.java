package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user_profiles")
public class DiscordUserProfileEntity {

    @Id
    @Column(name = "discord_user_id", nullable = false, updatable = false)
    private String discordUserId;

    @Column(name = "bma_username")
    private String bmaUsername;

    @Column(name = "bma_password")
    private String bmaPassword;

    @Column(name = "max_entry_time", nullable = false)
    private String maxEntryTime = "09:00";

    @Column(name = "jitter_minutes", nullable = false)
    private Integer jitterMinutes = 5;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DiscordUserProfileEntity() {
    }

    public DiscordUserProfileEntity(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    @PrePersist
    public void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordUserProfileEntity that = (DiscordUserProfileEntity) o;
        return Objects.equals(discordUserId, that.discordUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discordUserId);
    }
}
