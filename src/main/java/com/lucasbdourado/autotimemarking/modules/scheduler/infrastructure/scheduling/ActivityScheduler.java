package com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling;

import com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow;
import com.lucasbdourado.autotimemarking.modules.scheduler.domain.SchedulerTimezoneFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ActivityScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityScheduler.class);

    private final MarkingWorkflow markingWorkflow;
    private final SchedulerTimezoneFilter timezoneFilter;
    private final String timezone;

    public ActivityScheduler(
            MarkingWorkflow markingWorkflow,
            SchedulerTimezoneFilter timezoneFilter,
            @Value("${bmaquiosque.timezone}") String timezone
    ) {
        this.markingWorkflow = markingWorkflow;
        this.timezoneFilter = timezoneFilter;
        this.timezone = timezone;
    }

    @Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")
    public void execute() {
        try {
            ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of(timezone));

            if (!timezoneFilter.isWithinOperatingWindow(currentTime)) {
                LOGGER.info(
                        "Skipping marking cycle outside the operating window at {} on {}",
                        currentTime.toLocalTime(),
                        currentTime.getDayOfWeek()
                );
                return;
            }

            LOGGER.info("Starting marking cycle at {}", currentTime);
            markingWorkflow.executeMarkingCycle();
        } catch (Exception exception) {
            LOGGER.error("Activity scheduler cycle failed", exception);
        }
    }
}
