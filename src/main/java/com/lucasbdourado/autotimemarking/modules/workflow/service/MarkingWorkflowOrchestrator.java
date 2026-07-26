package com.lucasbdourado.autotimemarking.modules.workflow.service;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.calculation.service.MarkingCalculatorService;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port.DiscordUserProfileRepository;
import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationEvent;
import com.lucasbdourado.autotimemarking.modules.notification.domain.port.NotificationPort;
import com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
public class MarkingWorkflowOrchestrator implements MarkingWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkingWorkflowOrchestrator.class);

    private final TimeClockClient timeClockClient;
    private final MarkingCalculatorService calculatorService;
    private final BmaquiosqueProperties properties;
    private final NotificationPort notificationPort;
    private final DiscordUserProfileRepository userProfileRepository;

    public MarkingWorkflowOrchestrator(
            TimeClockClient timeClockClient,
            MarkingCalculatorService calculatorService,
            BmaquiosqueProperties properties,
            NotificationPort notificationPort,
            DiscordUserProfileRepository userProfileRepository
    ) {
        this.timeClockClient = timeClockClient;
        this.calculatorService = calculatorService;
        this.properties = properties;
        this.notificationPort = notificationPort;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void executeMarkingCycle() throws Exception {
        String timezoneStr = (properties.getTimezone() != null && !properties.getTimezone().isBlank())
                ? properties.getTimezone()
                : "America/Sao_Paulo";
        ZoneId zoneId = ZoneId.of(timezoneStr);
        LocalTime currentTime = ZonedDateTime.now(zoneId).toLocalTime();

        LOGGER.info("Starting marking workflow evaluation cycle at {} ({})", currentTime, timezoneStr);

        List<BmaquiosqueProperties> targetUserProperties = resolveTargetUserProperties();

        LOGGER.info("Executing marking evaluation cycle for {} user target(s)", targetUserProperties.size());

        for (BmaquiosqueProperties userProps : targetUserProperties) {
            executeCycleForUser(userProps, currentTime);
        }
    }

    private List<BmaquiosqueProperties> resolveTargetUserProperties() {
        List<DiscordUserProfile> activeProfiles = userProfileRepository.findAllActiveProfiles().stream()
                .filter(p -> p.getBmaUsername() != null && !p.getBmaUsername().isBlank())
                .filter(p -> p.getBmaPassword() != null && !p.getBmaPassword().isBlank())
                .collect(Collectors.toList());

        if (activeProfiles.isEmpty()) {
            LOGGER.info("No active database profiles found with valid credentials. Using default property configuration.");
            return List.of(properties);
        }

        List<BmaquiosqueProperties> list = new ArrayList<>();
        for (DiscordUserProfile profile : activeProfiles) {
            BmaquiosqueProperties userProps = new BmaquiosqueProperties();
            userProps.setUrl(properties.getUrl());
            userProps.setTimezone(properties.getTimezone());
            userProps.setSelectors(properties.getSelectors());
            userProps.setUsername(profile.getBmaUsername());
            userProps.setPassword(profile.getBmaPassword());
            userProps.setMaxEntryTime(profile.getMaxEntryTime());
            userProps.setJitterMinutes(profile.getJitterMinutes());
            list.add(userProps);
        }
        return list;
    }

    private void executeCycleForUser(BmaquiosqueProperties userProps, LocalTime currentTime) throws Exception {
        LOGGER.info("Evaluating marking cycle for user: {}", userProps.getUsername());

        List<LocalTime> dailyTimes;
        try {
            dailyTimes = timeClockClient.retrieveDailyMarkings(
                    userProps.getUsername(),
                    userProps.getPassword()
            );
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve daily markings for user {}: {}", userProps.getUsername(), e.getMessage(), e);
            notificationPort.sendNotification(
                    NotificationEvent.failure(
                            null,
                            "CONSULTA_PONTO",
                            "Erro ao consultar marcações do dia para usuário '" + userProps.getUsername() + "': " + e.getMessage(),
                            1
                    )
            );
            throw e;
        }

        if (dailyTimes == null) {
            dailyTimes = Collections.emptyList();
        }

        WorkdayState state = WorkdayState.fromTimes(dailyTimes);
        LOGGER.info("Current workday state for user {} contains {} registered markings", userProps.getUsername(), state.existingMarkings().size());

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, userProps);

        LOGGER.info(
                "Workflow evaluation result for user {}: shouldPunch={}, nextType={}, target={}, jitteredTarget={}, reason='{}'",
                userProps.getUsername(),
                decision.shouldPunch(),
                decision.nextType(),
                decision.calculatedTargetTime(),
                decision.jitteredTargetTime(),
                decision.reason()
        );

        if (decision.shouldPunch()) {
            LOGGER.info("Triggering punch registration for user {} stage: {}", userProps.getUsername(), decision.nextType());
            String stageName = decision.nextType() != null ? decision.nextType().name() : "MARCAÇÃO";
            try {
                timeClockClient.registerMarking(userProps.getUsername(), userProps.getPassword());
                LOGGER.info("Successfully registered punch for user {} stage: {}", userProps.getUsername(), decision.nextType());
                notificationPort.sendNotification(
                        NotificationEvent.success(
                                null,
                                stageName,
                                "Marcação registrada com sucesso via BMAquiosque para o usuário '" + userProps.getUsername() + "'."
                        )
                );
            } catch (Exception e) {
                LOGGER.error("Failed to register punch for user {} stage {}: {}", userProps.getUsername(), stageName, e.getMessage(), e);
                notificationPort.sendNotification(
                        NotificationEvent.failure(
                                null,
                                stageName,
                                "Erro ao registrar ponto para o usuário '" + userProps.getUsername() + "': " + e.getMessage(),
                                1
                        )
                );
                throw e;
            }
        } else {
            LOGGER.info("No punch required for user {} at this cycle: {}", userProps.getUsername(), decision.reason());
        }
    }
}
