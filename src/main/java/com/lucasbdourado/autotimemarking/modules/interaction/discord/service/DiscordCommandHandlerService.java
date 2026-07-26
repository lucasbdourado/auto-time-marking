package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordCommandHandlerService {

    private final Map<String, DiscordUserProfile> userProfiles = new ConcurrentHashMap<>();

    public DiscordUserProfile registerUser(String discordUserId) {
        return userProfiles.computeIfAbsent(discordUserId, DiscordUserProfile::new);
    }

    public Optional<DiscordUserProfile> findUser(String discordUserId) {
        return Optional.ofNullable(userProfiles.get(discordUserId));
    }

    public String setCredentials(String discordUserId, String bmaUsername, String bmaPassword) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setBmaUsername(bmaUsername);
        profile.setBmaPassword(bmaPassword);
        return "Credentials for BMAquiosque user '" + bmaUsername + "' updated successfully.";
    }

    public String configureSchedule(String discordUserId, String maxEntryTime, Integer jitterMinutes) {
        DiscordUserProfile profile = registerUser(discordUserId);
        if (maxEntryTime != null && !maxEntryTime.isBlank()) {
            profile.setMaxEntryTime(maxEntryTime);
        }
        if (jitterMinutes != null && jitterMinutes >= 0) {
            profile.setJitterMinutes(jitterMinutes);
        }
        return "Schedule updated: Max Entry Time = " + profile.getMaxEntryTime() +
                ", Jitter = " + profile.getJitterMinutes() + " minutes.";
    }

    public String pauseAutomation(String discordUserId) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setActive(false);
        return "Automation PAUSED for your user.";
    }

    public String resumeAutomation(String discordUserId) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setActive(true);
        return "Automation RESUMED for your user.";
    }

    public String getStatus(String discordUserId) {
        Optional<DiscordUserProfile> profileOpt = findUser(discordUserId);
        if (profileOpt.isEmpty()) {
            return "User not registered. Use /register to create your profile.";
        }
        DiscordUserProfile profile = profileOpt.get();
        return String.format(
                "User Status:\n- State: %s\n- BMA User: %s\n- Max Entry Time: %s\n- Jitter: %d min",
                profile.isActive() ? "ACTIVE" : "PAUSED",
                profile.getBmaUsername() != null ? profile.getBmaUsername() : "Not configured",
                profile.getMaxEntryTime(),
                profile.getJitterMinutes()
        );
    }
}
