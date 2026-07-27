package com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling;

import com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow;
import com.lucasbdourado.autotimemarking.modules.scheduler.domain.SchedulerTimezoneFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitySchedulerTest {

    @Mock
    private MarkingWorkflow markingWorkflow;

    @Mock
    private SchedulerTimezoneFilter timezoneFilter;

    private ActivityScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ActivityScheduler(markingWorkflow, timezoneFilter, "America/Sao_Paulo", true);
    }

    @Test
    void execute_whenInsideWindow_shouldExecuteWorkflow() throws Exception {
        when(timezoneFilter.isWithinOperatingWindow(any(ZonedDateTime.class))).thenReturn(true);

        scheduler.execute();

        verify(markingWorkflow).executeMarkingCycle();
    }

    @Test
    void execute_whenDisabled_shouldSkipWorkflow() throws Exception {
        ActivityScheduler disabledScheduler = new ActivityScheduler(markingWorkflow, timezoneFilter, "America/Sao_Paulo", false);

        disabledScheduler.execute();

        verify(markingWorkflow, never()).executeMarkingCycle();
    }

    @Test
    void execute_whenOutsideWindow_shouldSkipWorkflow() throws Exception {
        when(timezoneFilter.isWithinOperatingWindow(any(ZonedDateTime.class))).thenReturn(false);

        scheduler.execute();

        verify(markingWorkflow, never()).executeMarkingCycle();
    }

    @Test
    void execute_whenWorkflowThrowsException_shouldCatchAndLog() throws Exception {
        when(timezoneFilter.isWithinOperatingWindow(any(ZonedDateTime.class))).thenReturn(true);
        doThrow(new IllegalStateException("workflow failed"))
                .when(markingWorkflow)
                .executeMarkingCycle();

        assertThatCode(scheduler::execute).doesNotThrowAnyException();
        verify(markingWorkflow).executeMarkingCycle();
    }
}
