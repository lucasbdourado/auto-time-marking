package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record WorkdayState(List<MarkingRecord> existingMarkings) {
    public WorkdayState {
        Objects.requireNonNull(existingMarkings, "existingMarkings cannot be null");
        existingMarkings = Collections.unmodifiableList(new ArrayList<>(existingMarkings));
    }

    public boolean hasMarking(MarkingType type) {
        return existingMarkings.stream().anyMatch(m -> m.type() == type);
    }

    public Optional<MarkingRecord> getMarking(MarkingType type) {
        return existingMarkings.stream().filter(m -> m.type() == type).findFirst();
    }

    public static WorkdayState fromTimes(List<LocalTime> times) {
        if (times == null || times.isEmpty()) {
            return new WorkdayState(List.of());
        }
        List<MarkingRecord> records = new ArrayList<>();
        MarkingType[] types = MarkingType.values();
        for (int i = 0; i < times.size() && i < types.length; i++) {
            records.add(new MarkingRecord(types[i], times.get(i)));
        }
        return new WorkdayState(records);
    }
}
