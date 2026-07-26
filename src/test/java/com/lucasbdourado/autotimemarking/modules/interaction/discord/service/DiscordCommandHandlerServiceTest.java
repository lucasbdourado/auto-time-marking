package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.service.WorkdaySummaryService;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port.DiscordUserProfileRepository;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DiscordCommandHandlerServiceTest {

    private DiscordCommandHandlerService service;
    private DiscordUserProfileRepository repository;
    private StubTimeClockClient timeClockClient;
    private static final String USER_ID = "discord-123456";

    @BeforeEach
    void setUp() {
        repository = new InMemoryDiscordUserProfileRepository();
        timeClockClient = new StubTimeClockClient();
        WorkdaySummaryService summaryService = new WorkdaySummaryService();
        DiscordWorkdayEmbedBuilder embedBuilder = new DiscordWorkdayEmbedBuilder(summaryService);

        service = new DiscordCommandHandlerService(repository, timeClockClient, summaryService, embedBuilder);
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
        assertTrue(pauseMsg.contains("PAUSADA"));
        assertFalse(service.findUser(USER_ID).orElseThrow().isActive());

        String resumeMsg = service.resumeAutomation(USER_ID);
        assertTrue(resumeMsg.contains("RETOMADA"));
        assertTrue(service.findUser(USER_ID).orElseThrow().isActive());
    }

    @Test
    @DisplayName("Should return status summary for registered user")
    void shouldReturnUserStatus() {
        service.setCredentials(USER_ID, "alice.smith", "pass456");
        service.configureSchedule(USER_ID, "09:15", 3);

        String status = service.getStatus(USER_ID);

        assertTrue(status.contains("ATIVO"));
        assertTrue(status.contains("alice.smith"));
        assertTrue(status.contains("09:15"));
        assertTrue(status.contains("3 min"));
    }

    @Test
    @DisplayName("Should return unregistered message for unknown user status query")
    void shouldReturnUnregisteredMessage() {
        String status = service.getStatus("unknown-user");
        assertTrue(status.contains("Usuário não registrado"));
    }

    @Test
    @DisplayName("Should generate workday summary embed for user with credentials")
    void shouldGenerateWorkdaySummaryEmbed() throws Exception {
        service.setCredentials(USER_ID, "alice.smith", "pass456");
        timeClockClient.dailyMarkings = List.of(LocalTime.of(8, 0), LocalTime.of(12, 0));

        MessageEmbed embed = service.getWorkdaySummaryEmbed(USER_ID);

        assertNotNull(embed);
        assertTrue(embed.getTitle().contains("Resumo do ponto"));
    }

    @Test
    @DisplayName("Should register punch and return workday summary embed")
    void shouldPunchAndGetWorkdaySummaryEmbed() throws Exception {
        service.setCredentials(USER_ID, "alice.smith", "pass456");
        timeClockClient.dailyMarkings = List.of(LocalTime.of(8, 0));

        MessageEmbed embed = service.punchAndGetWorkdaySummaryEmbed(USER_ID);

        assertNotNull(embed);
        assertTrue(timeClockClient.punchRegistered);
        assertTrue(embed.getTitle().contains("Resumo do ponto"));
    }

    @Test
    @DisplayName("Should throw exception when getting summary embed for user without credentials")
    void shouldThrowExceptionWhenNoCredentials() {
        service.registerUser(USER_ID);

        assertThrows(IllegalArgumentException.class, () -> service.getWorkdaySummaryEmbed(USER_ID));
        assertThrows(IllegalArgumentException.class, () -> service.punchAndGetWorkdaySummaryEmbed(USER_ID));
    }

    private static class InMemoryDiscordUserProfileRepository implements DiscordUserProfileRepository {
        private final Map<String, DiscordUserProfile> map = new HashMap<>();

        @Override
        public DiscordUserProfile save(DiscordUserProfile userProfile) {
            map.put(userProfile.getDiscordUserId(), userProfile);
            return userProfile;
        }

        @Override
        public Optional<DiscordUserProfile> findByDiscordUserId(String discordUserId) {
            return Optional.ofNullable(map.get(discordUserId));
        }

        @Override
        public List<DiscordUserProfile> findAllActiveProfiles() {
            return map.values().stream().filter(DiscordUserProfile::isActive).collect(Collectors.toList());
        }

        @Override
        public void deleteByDiscordUserId(String discordUserId) {
            map.remove(discordUserId);
        }
    }

    private static class StubTimeClockClient implements TimeClockClient {
        List<LocalTime> dailyMarkings = List.of();
        boolean punchRegistered = false;

        @Override
        public List<LocalTime> retrieveDailyMarkings(String username, String password) {
            return dailyMarkings;
        }

        @Override
        public void registerMarking(String username, String password) {
            punchRegistered = true;
        }
    }
}
