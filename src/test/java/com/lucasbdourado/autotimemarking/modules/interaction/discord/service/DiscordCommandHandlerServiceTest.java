package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DiscordCommandHandlerServiceTest {

    private DiscordCommandHandlerService service;
    private static final String USER_ID = "discord-123456";

    @BeforeEach
    void setUp() {
        service = new DiscordCommandHandlerService();
    }

    @Test
    @DisplayName("Should register new Discord user profile")
    void shouldRegisterNewUser() {
        DiscordUserProfile profile = service.registerUser(USER_ID);

        assertNotNull(profile);
        assertEquals(USER_ID, profile.getDiscordUserId());
        assertTrue(profile.isActive());
        assertEquals("09:00", profile.getMaxEntryTime());
        assertEquals(5, profile.getJitterMinutes());
    }

    @Test
    @DisplayName("Should update BMAquiosque credentials for user")
    void shouldSetCredentials() {
        String result = service.setCredentials(USER_ID, "john.doe", "secret123");

        assertTrue(result.contains("john.doe"));
        Optional<DiscordUserProfile> profileOpt = service.findUser(USER_ID);
        assertTrue(profileOpt.isPresent());
        assertEquals("john.doe", profileOpt.get().getBmaUsername());
        assertEquals("secret123", profileOpt.get().getBmaPassword());
    }

    @Test
    @DisplayName("Should configure max entry time and jitter minutes")
    void shouldConfigureSchedule() {
        String result = service.configureSchedule(USER_ID, "08:30", 10);

        assertTrue(result.contains("08:30"));
        assertTrue(result.contains("10"));

        DiscordUserProfile profile = service.findUser(USER_ID).orElseThrow();
        assertEquals("08:30", profile.getMaxEntryTime());
        assertEquals(10, profile.getJitterMinutes());
    }

    @Test
    @DisplayName("Should pause and resume user automation")
    void shouldPauseAndResumeAutomation() {
        service.registerUser(USER_ID);

        String pauseMsg = service.pauseAutomation(USER_ID);
        assertTrue(pauseMsg.contains("PAUSED"));
        assertFalse(service.findUser(USER_ID).orElseThrow().isActive());

        String resumeMsg = service.resumeAutomation(USER_ID);
        assertTrue(resumeMsg.contains("RESUMED"));
        assertTrue(service.findUser(USER_ID).orElseThrow().isActive());
    }

    @Test
    @DisplayName("Should return status summary for registered user")
    void shouldReturnUserStatus() {
        service.setCredentials(USER_ID, "alice.smith", "pass456");
        service.configureSchedule(USER_ID, "09:15", 3);

        String status = service.getStatus(USER_ID);

        assertTrue(status.contains("ACTIVE"));
        assertTrue(status.contains("alice.smith"));
        assertTrue(status.contains("09:15"));
        assertTrue(status.contains("3 min"));
    }

    @Test
    @DisplayName("Should return unregistered message for unknown user status query")
    void shouldReturnUnregisteredMessage() {
        String status = service.getStatus("unknown-user");
        assertTrue(status.contains("User not registered"));
    }
}
