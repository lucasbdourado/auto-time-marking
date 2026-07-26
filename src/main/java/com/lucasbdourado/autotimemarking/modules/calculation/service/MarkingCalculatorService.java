package com.lucasbdourado.autotimemarking.modules.calculation.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingRecord;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.MarkingType;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.PunchDecision;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdayState;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;

@Service
public class MarkingCalculatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkingCalculatorService.class);
    private static final int WORK_MINUTES_PER_DAY = 525; // 8h45 = 525 min
    private static final int MAX_WORK_BEFORE_LUNCH_HOURS = 6;
    private static final int MIN_LUNCH_MINUTES = 60; // 1h

    private final TimeJitterService timeJitterService;

    public MarkingCalculatorService(TimeJitterService timeJitterService) {
        this.timeJitterService = timeJitterService;
    }

    public PunchDecision evaluateDecision(WorkdayState state, LocalTime currentTime, BmaquiosqueProperties properties) {
        int maxJitter = (properties != null && properties.getJitterMinutes() != null) ? properties.getJitterMinutes() : 0;
        int jitter = timeJitterService.generateJitterMinutes(maxJitter);
        return evaluateDecision(state, currentTime, properties, jitter);
    }

    public PunchDecision evaluateDecision(WorkdayState state, LocalTime currentTime, BmaquiosqueProperties properties, int jitterMinutes) {
        if (state == null || state.existingMarkings().isEmpty()) {
            return evaluateEntry(currentTime, properties, jitterMinutes);
        }

        int markingCount = state.existingMarkings().size();

        switch (markingCount) {
            case 0:
                return evaluateEntry(currentTime, properties, jitterMinutes);
            case 1:
                return evaluateLunchOut(state, currentTime, jitterMinutes);
            case 2:
                return evaluateLunchReturn(state, currentTime, jitterMinutes);
            case 3:
                return evaluateExit(state, currentTime, jitterMinutes);
            default:
                return PunchDecision.noPunch("Workday complete. All 4 markings registered for today.");
        }
    }

    private PunchDecision evaluateEntry(LocalTime currentTime, BmaquiosqueProperties properties, int jitterMinutes) {
        LocalTime maxEntryTime = parseMaxEntryTime(properties);
        LocalTime baseTarget = maxEntryTime;
        LocalTime jitteredTarget = baseTarget.plusMinutes(jitterMinutes);

        if (!currentTime.isBefore(jitteredTarget)) {
            return PunchDecision.execute(
                    MarkingType.ENTRY,
                    baseTarget,
                    jitteredTarget,
                    "Current time (" + currentTime + ") reaches or exceeds entry target time (" + jitteredTarget + ")."
            );
        }
        return PunchDecision.noPunch("Waiting for entry target time " + jitteredTarget + " (base: " + baseTarget + ").");
    }

    private PunchDecision evaluateLunchOut(WorkdayState state, LocalTime currentTime, int jitterMinutes) {
        MarkingRecord entryRecord = state.getMarking(MarkingType.ENTRY)
                .orElse(state.existingMarkings().get(0));

        LocalTime baseTarget = entryRecord.time().plusHours(MAX_WORK_BEFORE_LUNCH_HOURS);
        LocalTime jitteredTarget = baseTarget.plusMinutes(jitterMinutes);

        if (!currentTime.isBefore(jitteredTarget)) {
            return PunchDecision.execute(
                    MarkingType.LUNCH_OUT,
                    baseTarget,
                    jitteredTarget,
                    "Current time (" + currentTime + ") reaches or exceeds lunch-out target time (" + jitteredTarget + ")."
            );
        }
        return PunchDecision.noPunch("Waiting for lunch-out target time " + jitteredTarget + " (6h work limit).");
    }

    private PunchDecision evaluateLunchReturn(WorkdayState state, LocalTime currentTime, int jitterMinutes) {
        MarkingRecord lunchOutRecord = state.getMarking(MarkingType.LUNCH_OUT)
                .orElseGet(() -> state.existingMarkings().get(state.existingMarkings().size() - 1));

        LocalTime baseTarget = lunchOutRecord.time().plusMinutes(MIN_LUNCH_MINUTES);
        int effectiveJitter = Math.max(0, jitterMinutes);
        LocalTime jitteredTarget = baseTarget.plusMinutes(effectiveJitter);

        if (!currentTime.isBefore(jitteredTarget)) {
            return PunchDecision.execute(
                    MarkingType.LUNCH_RETURN,
                    baseTarget,
                    jitteredTarget,
                    "Current time (" + currentTime + ") reaches or exceeds lunch-return target time (" + jitteredTarget + ")."
            );
        }
        return PunchDecision.noPunch("Waiting for lunch-return target time " + jitteredTarget + " (min 1h lunch).");
    }

    private PunchDecision evaluateExit(WorkdayState state, LocalTime currentTime, int jitterMinutes) {
        MarkingRecord entryRecord = state.getMarking(MarkingType.ENTRY)
                .orElse(state.existingMarkings().get(0));
        MarkingRecord lunchOutRecord = state.getMarking(MarkingType.LUNCH_OUT)
                .orElse(state.existingMarkings().get(1));
        MarkingRecord lunchReturnRecord = state.getMarking(MarkingType.LUNCH_RETURN)
                .orElse(state.existingMarkings().get(2));

        Duration actualLunchDuration = Duration.between(lunchOutRecord.time(), lunchReturnRecord.time());
        LocalTime baseTarget = entryRecord.time().plus(actualLunchDuration).plusMinutes(WORK_MINUTES_PER_DAY);
        LocalTime jitteredTarget = baseTarget.plusMinutes(jitterMinutes);

        if (!currentTime.isBefore(jitteredTarget)) {
            return PunchDecision.execute(
                    MarkingType.EXIT,
                    baseTarget,
                    jitteredTarget,
                    "Current time (" + currentTime + ") reaches or exceeds exit target time (" + jitteredTarget + ")."
            );
        }
        return PunchDecision.noPunch("Waiting for exit target time " + jitteredTarget + " (recalculated work duration).");
    }

    private LocalTime parseMaxEntryTime(BmaquiosqueProperties properties) {
        if (properties != null && properties.getMaxEntryTime() != null && !properties.getMaxEntryTime().isBlank()) {
            try {
                return LocalTime.parse(properties.getMaxEntryTime().trim());
            } catch (Exception e) {
                LOGGER.warn("Failed to parse maxEntryTime '{}', defaulting to 09:00", properties.getMaxEntryTime());
            }
        }
        return LocalTime.of(9, 0);
    }
}
