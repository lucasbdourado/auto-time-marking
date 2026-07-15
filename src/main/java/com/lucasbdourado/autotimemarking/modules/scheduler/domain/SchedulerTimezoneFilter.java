package com.lucasbdourado.autotimemarking.modules.scheduler.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class SchedulerTimezoneFilter {

    private static final LocalTime START_TIME = LocalTime.of(5, 0, 0);
    private static final LocalTime END_TIME = LocalTime.of(22, 0, 0);

    public boolean isWithinOperatingWindow(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return false;
        }

        DayOfWeek dayOfWeek = zonedDateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime localTime = zonedDateTime.toLocalTime();
        return !localTime.isBefore(START_TIME) && !localTime.isAfter(END_TIME);
    }
}
