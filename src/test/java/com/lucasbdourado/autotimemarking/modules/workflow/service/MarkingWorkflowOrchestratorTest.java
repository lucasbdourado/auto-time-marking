package com.lucasbdourado.autotimemarking.modules.workflow.service;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingType;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.calculation.service.MarkingCalculatorService;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkingWorkflowOrchestratorTest {

    @Mock
    private TimeClockClient timeClockClient;

    @Mock
    private MarkingCalculatorService calculatorService;

    private BmaquiosqueProperties properties;
    private MarkingWorkflowOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        properties = new BmaquiosqueProperties();
        properties.setUsername("userTest");
        properties.setPassword("passTest");
        properties.setTimezone("America/Sao_Paulo");

        orchestrator = new MarkingWorkflowOrchestrator(timeClockClient, calculatorService, properties);
    }

    @Test
    @DisplayName("Should retrieve markings and execute punch when decision.shouldPunch is true")
    void executeMarkingCycle_shouldPunchTrue_callsRegisterMarking() throws Exception {
        List<LocalTime> times = List.of(LocalTime.of(8, 0));
        when(timeClockClient.retrieveDailyMarkings("userTest", "passTest")).thenReturn(times);

        PunchDecision decision = PunchDecision.execute(
                MarkingType.LUNCH_OUT,
                LocalTime.of(14, 0),
                LocalTime.of(14, 0),
                "Lunch time reached"
        );
        when(calculatorService.evaluateDecision(any(WorkdayState.class), any(LocalTime.class), eq(properties)))
                .thenReturn(decision);

        orchestrator.executeMarkingCycle();

        verify(timeClockClient).registerMarking("userTest", "passTest");
    }

    @Test
    @DisplayName("Should retrieve markings and NOT execute punch when decision.shouldPunch is false")
    void executeMarkingCycle_shouldPunchFalse_skipsRegisterMarking() throws Exception {
        List<LocalTime> times = List.of(LocalTime.of(8, 0));
        when(timeClockClient.retrieveDailyMarkings("userTest", "passTest")).thenReturn(times);

        PunchDecision decision = PunchDecision.noPunch("Waiting for lunch time");
        when(calculatorService.evaluateDecision(any(WorkdayState.class), any(LocalTime.class), eq(properties)))
                .thenReturn(decision);

        orchestrator.executeMarkingCycle();

        verify(timeClockClient, never()).registerMarking(any(), any());
    }
}
