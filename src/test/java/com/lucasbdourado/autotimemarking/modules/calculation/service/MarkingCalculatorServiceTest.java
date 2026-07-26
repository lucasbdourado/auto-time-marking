package com.lucasbdourado.autotimemarking.modules.calculation.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingRecord;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingType;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkingCalculatorServiceTest {

    private MarkingCalculatorService calculatorService;
    private BmaquiosqueProperties properties;

    @BeforeEach
    void setUp() {
        TimeJitterService jitterService = new TimeJitterService();
        calculatorService = new MarkingCalculatorService(jitterService);

        properties = new BmaquiosqueProperties();
        properties.setMaxEntryTime("09:00");
        properties.setJitterMinutes(0);
    }

    @Test
    @DisplayName("AC-001: Entry decision should trigger when current time reaches maxEntryTime")
    void evaluate_entryPunch_whenTimeReached() {
        WorkdayState state = new WorkdayState(List.of());
        LocalTime currentTime = LocalTime.of(9, 5);

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties, 0);

        assertThat(decision.shouldPunch()).isTrue();
        assertThat(decision.nextType()).isEqualTo(MarkingType.ENTRY);
        assertThat(decision.calculatedTargetTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    @DisplayName("AC-001: Entry decision should NOT trigger when current time is before maxEntryTime")
    void evaluate_entryPunch_whenTimeNotReached() {
        WorkdayState state = new WorkdayState(List.of());
        LocalTime currentTime = LocalTime.of(8, 45);

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties, 0);

        assertThat(decision.shouldPunch()).isFalse();
        assertThat(decision.nextType()).isNull();
    }

    @Test
    @DisplayName("AC-002: Lunch-Out decision should trigger at entry + 6h")
    void evaluate_lunchOutPunch_whenTimeReached() {
        WorkdayState state = new WorkdayState(List.of(
                new MarkingRecord(MarkingType.ENTRY, LocalTime.of(8, 0))
        ));
        LocalTime currentTime = LocalTime.of(14, 0);

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties, 0);

        assertThat(decision.shouldPunch()).isTrue();
        assertThat(decision.nextType()).isEqualTo(MarkingType.LUNCH_OUT);
        assertThat(decision.calculatedTargetTime()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("AC-003: Lunch-Return decision should trigger at lunch-out + 1h")
    void evaluate_lunchReturnPunch_whenTimeReached() {
        WorkdayState state = new WorkdayState(List.of(
                new MarkingRecord(MarkingType.ENTRY, LocalTime.of(8, 0)),
                new MarkingRecord(MarkingType.LUNCH_OUT, LocalTime.of(12, 0))
        ));
        LocalTime currentTime = LocalTime.of(13, 0);

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties, 0);

        assertThat(decision.shouldPunch()).isTrue();
        assertThat(decision.nextType()).isEqualTo(MarkingType.LUNCH_RETURN);
        assertThat(decision.calculatedTargetTime()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("AC-004: Recalculated Exit decision (08:00 entry, 12:00 out, 13:30 return -> 18:15 exit)")
    void evaluate_exitPunch_recalculatedWith1h30Lunch() {
        WorkdayState state = new WorkdayState(List.of(
                new MarkingRecord(MarkingType.ENTRY, LocalTime.of(8, 0)),
                new MarkingRecord(MarkingType.LUNCH_OUT, LocalTime.of(12, 0)),
                new MarkingRecord(MarkingType.LUNCH_RETURN, LocalTime.of(13, 30))
        ));
        LocalTime currentTime = LocalTime.of(18, 15);

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties, 0);

        assertThat(decision.shouldPunch()).isTrue();
        assertThat(decision.nextType()).isEqualTo(MarkingType.EXIT);
        assertThat(decision.calculatedTargetTime()).isEqualTo(LocalTime.of(18, 15));
    }

    @Test
    @DisplayName("AC-005: Completed Workday should return shouldPunch = false")
    void evaluate_completedWorkday_noPunch() {
        WorkdayState state = new WorkdayState(List.of(
                new MarkingRecord(MarkingType.ENTRY, LocalTime.of(8, 0)),
                new MarkingRecord(MarkingType.LUNCH_OUT, LocalTime.of(12, 0)),
                new MarkingRecord(MarkingType.LUNCH_RETURN, LocalTime.of(13, 0)),
                new MarkingRecord(MarkingType.EXIT, LocalTime.of(17, 45))
        ));
        LocalTime currentTime = LocalTime.of(18, 0);

        PunchDecision decision = calculatorService.evaluateDecision(state, currentTime, properties, 0);

        assertThat(decision.shouldPunch()).isFalse();
        assertThat(decision.reason()).contains("Workday complete");
    }
}
