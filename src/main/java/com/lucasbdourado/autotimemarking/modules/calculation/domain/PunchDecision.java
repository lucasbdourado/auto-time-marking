package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.time.LocalTime;
import java.util.Objects;

public record PunchDecision(
        boolean shouldPunch,
        MarkingType nextType,
        LocalTime calculatedTargetTime,
        LocalTime jitteredTargetTime,
        String reason
) {
    public PunchDecision {
        Objects.requireNonNull(reason, "reason cannot be null");
    }

    public static PunchDecision noPunch(String reason) {
        return new PunchDecision(false, null, null, null, reason);
    }

    public static PunchDecision execute(MarkingType type, LocalTime target, LocalTime jitteredTarget, String reason) {
        return new PunchDecision(true, type, target, jitteredTarget, reason);
    }
}
