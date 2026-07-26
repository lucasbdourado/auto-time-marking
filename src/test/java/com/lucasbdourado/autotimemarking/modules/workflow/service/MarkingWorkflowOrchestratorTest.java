package com.lucasbdourado.autotimemarking.modules.workflow.service;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingType;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.calculation.service.MarkingCalculatorService;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port.DiscordUserProfileRepository;
import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationType;
import com.lucasbdourado.autotimemarking.modules.notification.domain.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkingWorkflowOrchestratorTest {

    @Mock
    private TimeClockClient timeClockClient;

    @Mock
    private MarkingCalculatorService calculatorService;

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private DiscordUserProfileRepository userProfileRepository;

    private BmaquiosqueProperties properties;
    private MarkingWorkflowOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        properties = new BmaquiosqueProperties();
        properties.setUsername("userTest");
        properties.setPassword("passTest");
        properties.setTimezone("America/Sao_Paulo");

        lenient().when(userProfileRepository.findAllActiveProfiles()).thenReturn(Collections.emptyList());

        orchestrator = new MarkingWorkflowOrchestrator(
                timeClockClient,
                calculatorService,
                properties,
                notificationPort,
                userProfileRepository
        );
    }

    @Test
    @DisplayName("Should retrieve markings, execute punch, and dispatch success notification when decision.shouldPunch is true")
    void executeMarkingCycle_shouldPunchTrue_callsRegisterMarkingAndNotifies() throws Exception {
        List<LocalTime> times = List.of(LocalTime.of(8, 0));
        when(timeClockClient.retrieveDailyMarkings("userTest", "passTest")).thenReturn(times);

        PunchDecision decision = PunchDecision.execute(
                MarkingType.LUNCH_OUT,
                LocalTime.of(14, 0),
                LocalTime.of(14, 0),
                "Lunch time reached"
        );
        when(calculatorService.evaluateDecision(any(WorkdayState.class), any(LocalTime.class), any(BmaquiosqueProperties.class)))
                .thenReturn(decision);

        orchestrator.executeMarkingCycle();

        verify(timeClockClient).registerMarking("userTest", "passTest");
        verify(notificationPort).sendNotification(argThat(event ->
                event.type() == NotificationType.SUCCESS &&
                "LUNCH_OUT".equals(event.stageName())
        ));
    }

    @Test
    @DisplayName("Should retrieve markings and NOT execute punch or send notification when decision.shouldPunch is false")
    void executeMarkingCycle_shouldPunchFalse_skipsRegisterMarking() throws Exception {
        List<LocalTime> times = List.of(LocalTime.of(8, 0));
        when(timeClockClient.retrieveDailyMarkings("userTest", "passTest")).thenReturn(times);

        PunchDecision decision = PunchDecision.noPunch("Waiting for lunch time");
        when(calculatorService.evaluateDecision(any(WorkdayState.class), any(LocalTime.class), any(BmaquiosqueProperties.class)))
                .thenReturn(decision);

        orchestrator.executeMarkingCycle();

        verify(timeClockClient, never()).registerMarking(any(), any());
        verify(notificationPort, never()).sendNotification(any());
    }

    @Test
    @DisplayName("Should dispatch failure notification when punch registration throws an exception")
    void executeMarkingCycle_punchFailure_dispatchesFailureNotification() throws Exception {
        List<LocalTime> times = List.of(LocalTime.of(8, 0));
        when(timeClockClient.retrieveDailyMarkings("userTest", "passTest")).thenReturn(times);

        PunchDecision decision = PunchDecision.execute(
                MarkingType.EXIT,
                LocalTime.of(18, 0),
                LocalTime.of(18, 0),
                "Exit time reached"
        );
        when(calculatorService.evaluateDecision(any(WorkdayState.class), any(LocalTime.class), any(BmaquiosqueProperties.class)))
                .thenReturn(decision);

        doThrow(new RuntimeException("BMA Quiosque unavailable"))
                .when(timeClockClient).registerMarking("userTest", "passTest");

        assertThrows(RuntimeException.class, () -> orchestrator.executeMarkingCycle());

        verify(notificationPort).sendNotification(argThat(event ->
                event.type() == NotificationType.FAILURE &&
                "EXIT".equals(event.stageName()) &&
                event.message().contains("BMA Quiosque unavailable")
        ));
    }

    @Test
    @DisplayName("Should execute marking cycle for active database user profiles")
    void executeMarkingCycle_withActiveDatabaseProfiles_executesCycleForEachProfile() throws Exception {
        DiscordUserProfile profile1 = new DiscordUserProfile("discord-1");
        profile1.setBmaUsername("user1");
        profile1.setBmaPassword("pass1");

        DiscordUserProfile profile2 = new DiscordUserProfile("discord-2");
        profile2.setBmaUsername("user2");
        profile2.setBmaPassword("pass2");

        when(userProfileRepository.findAllActiveProfiles()).thenReturn(List.of(profile1, profile2));
        when(timeClockClient.retrieveDailyMarkings(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(calculatorService.evaluateDecision(any(WorkdayState.class), any(LocalTime.class), any(BmaquiosqueProperties.class)))
                .thenReturn(PunchDecision.noPunch("No punch required"));

        orchestrator.executeMarkingCycle();

        verify(timeClockClient).retrieveDailyMarkings("user1", "pass1");
        verify(timeClockClient).retrieveDailyMarkings("user2", "pass2");
    }
}
