package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.persistence;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port.DiscordUserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(DiscordUserProfileRepositoryAdapter.class)
class DiscordUserProfileRepositoryTest {


    @Autowired
    private DiscordUserProfileRepository repository;

    @Test
    @DisplayName("Should save and retrieve Discord user profile from database")
    void shouldSaveAndRetrieveUserProfile() {
        DiscordUserProfile profile = new DiscordUserProfile("discord-999");
        profile.setBmaUsername("user.h2");
        profile.setBmaPassword("pass.h2");
        profile.setMaxEntryTime("08:45");
        profile.setJitterMinutes(8);
        profile.setActive(true);

        repository.save(profile);

        Optional<DiscordUserProfile> found = repository.findByDiscordUserId("discord-999");
        assertTrue(found.isPresent());
        assertEquals("discord-999", found.get().getDiscordUserId());
        assertEquals("user.h2", found.get().getBmaUsername());
        assertEquals("pass.h2", found.get().getBmaPassword());
        assertEquals("08:45", found.get().getMaxEntryTime());
        assertEquals(8, found.get().getJitterMinutes());
        assertTrue(found.get().isActive());
    }

    @Test
    @DisplayName("Should query active profiles only")
    void shouldFindActiveProfilesOnly() {
        DiscordUserProfile active1 = new DiscordUserProfile("discord-active-1");
        active1.setBmaUsername("active1");
        active1.setActive(true);

        DiscordUserProfile paused = new DiscordUserProfile("discord-paused");
        paused.setBmaUsername("paused");
        paused.setActive(false);

        repository.save(active1);
        repository.save(paused);

        List<DiscordUserProfile> activeList = repository.findAllActiveProfiles();

        assertEquals(1, activeList.size());
        assertEquals("discord-active-1", activeList.get(0).getDiscordUserId());
    }
}
