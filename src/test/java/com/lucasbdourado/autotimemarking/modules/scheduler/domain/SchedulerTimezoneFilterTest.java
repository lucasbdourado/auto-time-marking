package com.lucasbdourado.autotimemarking.modules.scheduler.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerTimezoneFilterTest {

    private static final ZoneId TIMEZONE = ZoneId.of("America/Sao_Paulo");

    private final SchedulerTimezoneFilter timezoneFilter = new SchedulerTimezoneFilter();

    @ParameterizedTest(name = "{0}")
    @MethodSource("operatingWindowScenarios")
    void shouldEvaluateOperatingWindow(String scenario, ZonedDateTime dateTime, boolean expected) {
        assertThat(timezoneFilter.isWithinOperatingWindow(dateTime)).isEqualTo(expected);
    }

    @Test
    void shouldReturnFalseWhenDateTimeIsNull() {
        assertThat(timezoneFilter.isWithinOperatingWindow(null)).isFalse();
    }

    private static Stream<Arguments> operatingWindowScenarios() {
        return Stream.of(
                Arguments.of("Monday at 05:59 is outside", dateTime(13, 5, 59), false),
                Arguments.of("Monday at 06:00 is inside", dateTime(13, 6, 0), true),
                Arguments.of("Wednesday at 12:00 is inside", dateTime(15, 12, 0), true),
                Arguments.of("Monday at 22:00 is inside", dateTime(13, 22, 0), true),
                Arguments.of("Monday at 22:01 is outside", dateTime(13, 22, 1), false),
                Arguments.of("Saturday is outside", dateTime(18, 12, 0), false),
                Arguments.of("Sunday is outside", dateTime(19, 12, 0), false)
        );
    }

    private static ZonedDateTime dateTime(int dayOfMonth, int hour, int minute) {
        return ZonedDateTime.of(2026, 7, dayOfMonth, hour, minute, 0, 0, TIMEZONE);
    }
}
