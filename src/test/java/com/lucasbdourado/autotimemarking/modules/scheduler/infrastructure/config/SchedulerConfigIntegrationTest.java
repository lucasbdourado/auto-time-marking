package com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config;

import com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "bmaquiosque.username=test-user",
        "bmaquiosque.password=test-password",
        "bmaquiosque.max-entry-time=09:00",
        "bmaquiosque.jitter-minutes=5",
        "bmaquiosque.timezone=America/Sao_Paulo"
})
class SchedulerConfigIntegrationTest {

    @MockitoBean
    private MarkingWorkflow markingWorkflow;

    @Autowired
    private TaskScheduler taskScheduler;

    @Test
    void shouldConfigureTaskSchedulerCorrectly() {
        assertThat(taskScheduler)
                .isNotNull()
                .isInstanceOf(ThreadPoolTaskScheduler.class);

        ThreadPoolTaskScheduler poolScheduler = (ThreadPoolTaskScheduler) taskScheduler;

        assertThat(poolScheduler.getPoolSize()).isEqualTo(1);
        assertThat(poolScheduler.getThreadNamePrefix()).isEqualTo("activity-scheduler-");
    }
}
