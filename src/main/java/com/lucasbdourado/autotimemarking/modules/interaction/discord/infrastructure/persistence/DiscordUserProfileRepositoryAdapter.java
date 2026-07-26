package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.persistence;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port.DiscordUserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DiscordUserProfileRepositoryAdapter implements DiscordUserProfileRepository {

    private final SpringDataDiscordUserProfileRepository jpaRepository;

    public DiscordUserProfileRepositoryAdapter(SpringDataDiscordUserProfileRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DiscordUserProfile save(DiscordUserProfile userProfile) {
        DiscordUserProfileEntity entity = toEntity(userProfile);
        DiscordUserProfileEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<DiscordUserProfile> findByDiscordUserId(String discordUserId) {
        return jpaRepository.findById(discordUserId).map(this::toDomain);
    }

    @Override
    public List<DiscordUserProfile> findAllActiveProfiles() {
        return jpaRepository.findByActiveTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDiscordUserId(String discordUserId) {
        jpaRepository.deleteById(discordUserId);
    }

    private DiscordUserProfile toDomain(DiscordUserProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        DiscordUserProfile profile = new DiscordUserProfile(entity.getDiscordUserId());
        profile.setBmaUsername(entity.getBmaUsername());
        profile.setBmaPassword(entity.getBmaPassword());
        if (entity.getMaxEntryTime() != null) {
            profile.setMaxEntryTime(entity.getMaxEntryTime());
        }
        if (entity.getJitterMinutes() != null) {
            profile.setJitterMinutes(entity.getJitterMinutes());
        }
        profile.setActive(entity.isActive());
        return profile;
    }

    private DiscordUserProfileEntity toEntity(DiscordUserProfile profile) {
        if (profile == null) {
            return null;
        }
        DiscordUserProfileEntity entity = jpaRepository.findById(profile.getDiscordUserId())
                .orElseGet(() -> new DiscordUserProfileEntity(profile.getDiscordUserId()));

        entity.setBmaUsername(profile.getBmaUsername());
        entity.setBmaPassword(profile.getBmaPassword());
        entity.setMaxEntryTime(profile.getMaxEntryTime() != null ? profile.getMaxEntryTime() : "09:00");
        entity.setJitterMinutes(profile.getJitterMinutes() != null ? profile.getJitterMinutes() : 5);
        entity.setActive(profile.isActive());
        return entity;
    }
}
