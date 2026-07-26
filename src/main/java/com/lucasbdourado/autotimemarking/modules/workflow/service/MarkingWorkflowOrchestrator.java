package com.lucasbdourado.autotimemarking.modules.workflow.service;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.calculation.service.MarkingCalculatorService;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Primary
public class MarkingWorkflowOrchestrator implements MarkingWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkingWorkflowOrchestrator.class);

    private final TimeClockClient timeClockClient;
    private final MarkingCalculatorService calculatorService;
    private final BmaquiosqueProperties properties;

    public MarkingWorkflowOrchestrator(
            TimeClockClient timeClockClient,
            MarkingCalculatorService calculatorService,
            BmaquiosqueProperties properties
    ) {
        this.timeClockClient = timeClockClient;
        this.calculatorService = calculatorService;
        this.properties = properties;
    }

    @Override
    public void executeMarkingCycle() throws Exception {
        String timezoneStr = (properties.getTimezone() != null && !properties.getTimezone().isBlank())
                ? properties.getTimezone()
                : "America/Sao_Paulo";
        ZoneId zoneId = ZoneId.of(timezoneStr);
        LocalTime currentTime = ZonedDateTime.now(zoneId).toLocalTime();

        LOGGER.info("Starting marking workflow evaluation cycle at {} ({})", currentTime, timezoneStr);

        List<LocalTime> dailyTimes = timeClockClient.retrieveDailyMarkings(
                properties.getUsername(),
                properties.getPassword()
        );

        if (dailyTimes == null) {
            dailyTimes = Collections.emptyList();
        }

        WorkdayState state = WorkdayState.fromTimes(dailyTimes);
        LOGGER.info("Current workday state contains {} registered markings", state.existingMarkings().size());

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties);

        LOGGER.info(
                "Workflow evaluation result: shouldPunch={}, nextType={}, target={}, jitteredTarget={}, reason='{}'",
                decision.shouldPunch(),
                decision.nextType(),
                decision.calculatedTargetTime(),
                decision.jitteredTargetTime(),
                decision.reason()
        );

        if (decision.shouldPunch()) {
            LOGGER.info("Triggering punch registration for stage: {}", decision.nextType());
            timeClockClient.registerMarking(properties.getUsername(), properties.getPassword());
            LOGGER.info("Successfully registered punch for stage: {}", decision.nextType());
        } else {
            LOGGER.info("No punch required at this cycle: {}", decision.reason());
        }
    }
}
