package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataDiscordUserProfileRepository extends JpaRepository<DiscordUserProfileEntity, String> {

    List<DiscordUserProfileEntity> findByActiveTrue();
}
