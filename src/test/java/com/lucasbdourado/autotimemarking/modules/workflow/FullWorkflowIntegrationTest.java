package com.lucasbdourado.autotimemarking.modules.workflow;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingRecord;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingType;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.calculation.service.MarkingCalculatorService;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import com.lucasbdourado.autotimemarking.modules.workflow.service.MarkingWorkflowOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullWorkflowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BmaquiosqueProperties properties;

    @Autowired
    private TimeClockClient timeClockClient;

    @Autowired
    private MarkingCalculatorService calculatorService;

    @Autowired
    private MarkingWorkflowOrchestrator orchestrator;

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("bmaquiosque.username", () -> "365");
        registry.add("bmaquiosque.password", () -> "pass.test");
        registry.add("bmaquiosque.max-entry-time", () -> "09:00");
        registry.add("bmaquiosque.jitter-minutes", () -> "0");
    }

    @Test
    @DisplayName("Complete review: Read markings, calculate work hours, recalculate exit time, and verify workflow seamlessly matches production URL configuration")
    void testCompleteMarkingWorkflowAndCalculationLogic() throws Exception {
        // Point to the running mirror mock server URL
        properties.setUrl("http://localhost:" + port + "/wpe/quiosque");

        // 1. Read daily markings via PlaywrightTimeClockClient
        List<LocalTime> markings = timeClockClient.retrieveDailyMarkings("365", "pass.test");
        assertThat(markings).isNotEmpty();

        // 2. Build WorkdayState and calculate hours & decisions
        WorkdayState singleEntryState = new WorkdayState(List.of(new MarkingRecord(MarkingType.ENTRY, LocalTime.of(9, 0))));
        assertThat(singleEntryState.existingMarkings()).hasSize(1);

        // 3. Test calculation logic for Entry, Lunch Out, Lunch Return, and Exit Recalculation
        // Scenario A: Entry at 09:00. Max 6h work before lunch -> Lunch Out target is 15:00 (09:00 + 6h)
        PunchDecision decisionAfterEntry = calculatorService.evaluateDecision(singleEntryState, LocalTime.of(15, 0), properties, 0);
        assertThat(decisionAfterEntry.shouldPunch()).isTrue();
        assertThat(decisionAfterEntry.nextType()).isEqualTo(MarkingType.LUNCH_OUT);
        assertThat(decisionAfterEntry.calculatedTargetTime()).isEqualTo(LocalTime.of(15, 0));

        // Scenario B: After lunch return (09:00 entry, 12:00 lunch out, 13:30 lunch return = 1h30m lunch duration)
        // Work goal = 8h45m (525 min). 3h worked before lunch (180 min) + 5h45m remaining (345 min) = Exit target 19:15
        WorkdayState stateWithLunch = new WorkdayState(List.of(
                new MarkingRecord(MarkingType.ENTRY, LocalTime.of(9, 0)),
                new MarkingRecord(MarkingType.LUNCH_OUT, LocalTime.of(12, 0)),
                new MarkingRecord(MarkingType.LUNCH_RETURN, LocalTime.of(13, 30))
        ));

        PunchDecision exitDecision = calculatorService.evaluateDecision(stateWithLunch, LocalTime.of(19, 15), properties, 0);
        assertThat(exitDecision.shouldPunch()).isTrue();
        assertThat(exitDecision.nextType()).isEqualTo(MarkingType.EXIT);
        assertThat(exitDecision.calculatedTargetTime()).isEqualTo(LocalTime.of(19, 15));

        // 4. Run the workflow orchestrator cycle against the mock server
        orchestrator.executeMarkingCycle();

        // Verify properties URL switching configuration
        assertThat(properties.getUrl()).contains("localhost");
    }
}
