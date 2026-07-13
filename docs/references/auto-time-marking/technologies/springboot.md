# Technology Reference: Spring Boot

## Status

Status: Captured

Last updated: 2026-07-13

Captured by: Antigravity

## Technology Decision Reference

Related technology definition: docs/architecture/auto-time-marking/technology-definition.md

Decision area: Backend Runtime Core Framework

Decision status: Confirmed by user

## Why This Technology Was Selected

Spring Boot is the standard framework for enterprise Java development. For the Auto Time Marking project, it provides out-of-the-box scheduling via Spring Task Scheduling, externalized configuration properties parsing, SLF4J logging configuration, and a clear path for future expansion:
- Phase 2: Adding Spring Web / Discord Bot integrations.
- Phase 3: Exposing REST endpoints to integrate with a React web dashboard.

## Official Documentation Sources

| Source | URL or Context7 Library ID | Notes |
| --- | --- | --- |
| Context7 | `/spring-projects/spring-boot/v3.4.1` | Spring Boot official repository |
| Spring Boot Reference | https://spring.io/projects/spring-boot | Official project site |

## Context7 Notes

Spring Boot provides the `@Scheduled` annotation to run task loops.
We can customize the thread pool size and thread names of the default `ThreadPoolTaskScheduler` using standard properties:
- `spring.task.scheduling.thread-name-prefix=scheduling-`
- `spring.task.scheduling.pool.size=1` (appropriate for a single-user MVP)

It supports loading configuration files from custom locations or command-line parameters (e.g., `--spring.config.location=file:./config/application.properties`).

## Relevant Concepts for This Project

- **Scheduling**: Using `@EnableScheduling` on the main application class and `@Scheduled(fixedDelay = 1800000)` (30 minutes) on the check cycle worker.
- **External Configuration**: Binding properties to a Java record or class using `@ConfigurationProperties("bmaquiosque")` or `@Value`.
- **Dependency Injection**: Decoupling calculations, browser automation, and scheduling using Spring bean dependencies (`@Component`, `@Service`).

## Usage Guidelines for This Project

- Enable scheduling using `@EnableScheduling` on a configuration class.
- Keep `@Scheduled` methods thin; delegate business logic to service components.
- Bind all BMAquiosque parameters inside a dedicated configuration class, validating them on startup using standard validation or logical checks in a `@PostConstruct` block.
- Map sensitive credentials to environment variables:
  ```properties
  bmaquiosque.username=${BMAQUIOSQUE_USERNAME}
  bmaquiosque.password=${BMAQUIOSQUE_PASSWORD}
  ```

## Examples or Patterns to Follow

### Custom Task Scheduling Thread Pool
Configure in `application.properties`:
```properties
spring.task.scheduling.thread-name-prefix=auto-time-scheduler-
spring.task.scheduling.pool.size=1
spring.task.scheduling.shutdown.await-termination=true
spring.task.scheduling.shutdown.await-termination-period=30s
```

### Simple Scheduler Bean
```java
@Component
public class MarkingScheduler {
    private final AutomationService automationService;

    public MarkingScheduler(AutomationService automationService) {
        this.automationService = automationService;
    }

    @Scheduled(fixedDelayString = "${scheduling.interval-ms:1800000}")
    public void runAutomationCycle() {
        // Enforce 05:00-22:00 window and Mon-Fri days
        // Execute automation check
    }
}
```

## Risks or Caveats

- **Task Overlap**: If a task execution takes longer than the schedule interval, it might overlap if scheduled with `fixedRate`. We should use `fixedDelay` or ensure the thread pool is size-restricted to prevent simultaneous overlapping runs of the automation cycle.
- **Silent Thread Death**: Exceptions thrown inside a `@Scheduled` method must be caught and logged, otherwise they may stop subsequent runs of that scheduled task.

## Related Harness Documents

| Document | Path | Relationship |
| --- | --- | --- |
| Technology Definition | [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md) | Source decision |
