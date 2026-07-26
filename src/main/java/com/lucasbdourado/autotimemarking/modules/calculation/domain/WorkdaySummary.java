package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record WorkdaySummary(
        LocalDate date,
        List<LocalTime> markings,
        long workedMinutes,
        long remainingMinutes,
        LocalTime estimatedExitTime,
        LocalTime maxExitTime,
        LocalTime maxLunchReturnTime,
        String status
) {
    public WorkdaySummary {
        if (markings != null) {
            markings = Collections.unmodifiableList(new ArrayList<>(markings));
        } else {
            markings = Collections.emptyList();
        }
    }

    public WorkdaySummary(
            LocalDate date,
            List<LocalTime> markings,
            long workedMinutes,
            long remainingMinutes,
            LocalTime estimatedExitTime,
            String status
    ) {
        this(date, markings, workedMinutes, remainingMinutes, estimatedExitTime, null, null, status);
    }
}
