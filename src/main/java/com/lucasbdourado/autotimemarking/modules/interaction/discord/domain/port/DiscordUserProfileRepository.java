package com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;

import java.util.List;
import java.util.Optional;

public interface DiscordUserProfileRepository {

    DiscordUserProfile save(DiscordUserProfile userProfile);

    Optional<DiscordUserProfile> findByDiscordUserId(String discordUserId);

    List<DiscordUserProfile> findAllActiveProfiles();

    void deleteByDiscordUserId(String discordUserId);
}
