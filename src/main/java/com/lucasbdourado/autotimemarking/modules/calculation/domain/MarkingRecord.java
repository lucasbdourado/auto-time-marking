package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.time.LocalTime;
import java.util.Objects;

public record MarkingRecord(MarkingType type, LocalTime time) {
    public MarkingRecord {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(time, "time cannot be null");
    }
}
