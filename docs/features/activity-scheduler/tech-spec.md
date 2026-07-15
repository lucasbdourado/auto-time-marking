# Feature Technical Specification: activity-scheduler

## Status

Status: Confirmed

Last updated: 2026-07-15

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Feature Reference

`docs/features/activity-scheduler/feature.md`

Target output path: `docs/features/activity-scheduler/tech-spec.md`

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Feature | [feature.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/activity-scheduler/feature.md) | Feature | Confirmed | Primary feature source |
| Project Planning | [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/planning/auto-time-marking/project-planning.md) | Planning | Confirmed | MVP context, phases, dependencies |
| Technology Definition | [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md) | Technology definition | Confirmed | Confirmed stack and constraints |

## Specification Scope

This specification defines the scheduling engine architecture, operating windows (days and hours), timezone-aware validation logic, thread-safety, failure handling, and the interface for triggering the marking automation workflow.

## Feature Summary

An internal background runner executed periodically to check whether the system is inside the operating limits (Monday to Friday, 05:00 - 22:00 in the configured timezone) and, if so, trigger the marking check/automation workflow.

## Feature Goal

Establish an internal background scheduling loop that triggers a workday status check and marking execution cycle every 30 minutes, restricted to the operating hours of 05:00 to 22:00, Monday to Friday.

## Product Completion Criteria

- [x] Scheduler runs at 30-minute intervals.
- [x] Active time limits (05:00-22:00) and days (Mon-Fri) are verified before triggering the automation flow.
- [x] Scheduler thread recovery is verified (exceptions in execution do not kill scheduler).

## Technical Goals

- Enable Spring Boot scheduling framework utilizing `@EnableScheduling` and `@Scheduled`.
- Configure a dedicated task scheduler thread pool (size 1) to isolate scheduler threads and guarantee predictable behavior.
- Implement a timezone-aware execution filter that checks the day and time using `java.time` against `bmaquiosque.timezone`.
- Isolate error boundaries so that exceptions thrown during the marking check cycle do not crash or stall the scheduler.

## Non-Goals

- Scheduling multiple users across different timezones (the system operates on a single configured timezone at a time for the MVP).
- Executing marking calculations or direct browser interactions within the scheduler class itself (delegated to separate modules).
- Configurable schedule intervals via database/runtime API (MVP uses a static configuration).

## Confirmed Technology Decisions

| Area | Decision | Source | Applies To | Notes |
| --- | --- | --- | --- | --- |
| Language & Runtime | Java 21 | `technology-definition.md` | Whole project | Target JVM version |
| Framework | Spring Boot 3.4.x | `technology-definition.md` | Whole project | Application runtime |
| Scheduling | Spring Task Scheduling | `technology-definition.md` | `activity-scheduler` | `@EnableScheduling` and `@Scheduled` |
| Configuration | `application.properties` | `technology-definition.md` | Configuration loading | Properties binding via `BmaquiosqueProperties` |
| Logging | SLF4J + Logback | `technology-definition.md` | All logging | Auditing and diagnostic logging |
| Testing | JUnit 5 + Mockito | `technology-definition.md` | Unit/Integration Tests | Testing logic correctness |

## Pending Technology Decisions

| Area | Pending Decision | Impact on Feature | Required Next Step |
| --- | --- | --- | --- |
| None | None | None | None |

## Applicable Guidelines and References

| Reference | Path | Applies To | Usage |
| --- | --- | --- | --- |
| Java Guidelines | [.agents/docs/architecture/coding-guidelines/README.md](file:///.agents/docs/architecture/coding-guidelines/README.md) | Whole project | Code style, architecture layout, and pattern choices |

## Proposed Technical Approach

The `activity-scheduler` feature will be structured as a dedicated package `com.lucasbdourado.autotimemarking.modules.scheduler`.

### 1. Scheduler Thread Configuration
By default, Spring Boot uses a single-threaded TaskScheduler. To make this explicit and ensure proper thread naming prefix (e.g. `activity-scheduler-`), we will define a `SchedulerConfig` class.

### 2. Timezone-Aware Filter Logic
The scheduler must check if the current time in the configured timezone is:
1. Between Monday and Friday (inclusive).
2. Between 05:00 and 22:00 (inclusive).

This will be accomplished using `java.time` API:
- `ZonedDateTime.now(ZoneId.of(timezone))` to obtain current local time.
- Check if day of week is not Saturday/Sunday.
- Check if local time is not before 05:00 and not after 22:00.

### 3. Execution Interface
The scheduler will autowire an interface representing the marking execution workflow (e.g., `MarkingWorkflow`). This decouples the scheduler from the browser automation and calculations logic.

### 4. Scheduler Failure Safety
The scheduled method will wrap the workflow invocation in a `try-catch (Exception e)` block. Any exception will be logged at `ERROR` level. This prevents thread death and ensures the scheduling loop continues running at the next interval.

## Architecture Notes

- **Package Layout**:
  - `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config` (contains Spring `@Configuration` files)
  - `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling` (contains `@Component` scheduler classes)
  - `com.lucasbdourado.autotimemarking.modules.scheduler.domain` (contains interfaces or shared exceptions)
- **Dependency Flow**: The scheduler depends on the Configuration module (`BmaquiosqueProperties`) to read timezone details, and defines/depends on `MarkingWorkflow` to execute the cycle. Downstream modules (automation and calculation) will implement the workflow.

## Modules and Responsibilities

| Module or Component | Responsibility | Inputs | Outputs | Notes |
| --- | --- | --- | --- | --- |
| `SchedulerConfig` | Enable scheduling and define the custom thread pool for tasks | None (Spring Beans container) | `TaskScheduler` bean | Sets thread name prefix to `activity-scheduler-` |
| `ActivityScheduler` | The background loop executing every 30 minutes; filters time/day and triggers work | None (Triggered by Spring) | None (Void) | Uses `fixedDelay` to prevent overlapping runs |
| `MarkingWorkflow` | Interface representing the time-marking cycle execution | None | None | Implemented by downstream modules |

## Integration Contracts

| Producer | Consumer | Contract | Notes |
| --- | --- | --- | --- |
| `ActivityScheduler` | `MarkingWorkflow` | `void executeMarkingCycle()` | Direct invocation on active window match |

## Data Model

`Not applicable` — This feature does not persist data or manage domain models.

## Data Contracts

`Not applicable` — This feature does not exchange payloads or data DTOs.

## API or Interface Design

### `MarkingWorkflow` Interface

```java
package com.lucasbdourado.autotimemarking.modules.scheduler.domain;

public interface MarkingWorkflow {
    /**
     * Executes one check and marking cycle on BMAquiosque.
     * Throws Exception on failure to allow scheduler to track errors.
     */
    void executeMarkingCycle() throws Exception;
}
```

## State and Error Handling

| State or Error | Trigger | Expected Behavior | User/System Feedback | Notes |
| --- | --- | --- | --- | --- |
| Inside Window | Scheduler fires and time is within limits | Logs start info, triggers `MarkingWorkflow` | Logs cycle progress | Main operational flow |
| Outside Window | Scheduler fires but time/day is outside limits | Logs skip reason, does not trigger workflow | Logs skip message | Safe skip, thread sleeps |
| Workflow Error | `MarkingWorkflow` throws an exception | Catch exception, log error details and stack trace | Logs error message; scheduler recovers | Thread safety gate |

## Validation Rules

| Validation | Applies To | Enforcement Point | Error Behavior | Notes |
| --- | --- | --- | --- | --- |
| Day of Week | Current day in configured timezone | `ActivityScheduler` (Check check) | Skip cycle, log details | Allowed: Monday to Friday |
| Time Window | Current local time in configured timezone | `ActivityScheduler` (Check check) | Skip cycle, log details | Allowed: 05:00:00 to 22:00:00 |

## Security and Permissions

`Not applicable` — The scheduler runs entirely as a backend worker using pre-configured system parameters. No user authentication or privilege checks are executed here.

## Observability and Logging

Logs must use the configured masked converter format.

| Signal | Purpose | Source | Consumer | Notes |
| --- | --- | --- | --- | --- |
| `INFO` log | System status notification on cycle start, skip, or successful completion | `ActivityScheduler` | Log files / Console | Indicates scheduler status |
| `ERROR` log | Exception traces from failed execution cycles | `ActivityScheduler` | Log files / Console | Crucial for scheduler thread health monitoring |

## Performance Considerations

- **Single Thread Isolation**: Configuring the task scheduler thread pool size to 1 guarantees that scheduling resources are not wasted and only one scheduler job runs at a time.
- **Fixed Delay Policy**: Using `@Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")` instead of a fixed rate avoids queueing execution tasks if a previous cycle runs longer than 30 minutes (e.g. due to retry delays or slow browser automation page loads).

## Compatibility and Migration Notes

`Not applicable` — This is a greenfield project setup.

## Testing Strategy

| Test Type | What to Validate | Required? | Notes |
| --- | --- | --- | --- |
| Unit | Validate day/time filter logic inside scheduler under all boundary conditions (e.g., Saturday, Sunday, Mon 04:59, Mon 05:00, Mon 22:00, Mon 22:01). | Yes | Needs a mockable Clock or DateTime supplier to isolate system time |
| Unit | Verify that exceptions thrown by the `MarkingWorkflow` are caught and logged, and do not propagate to kill the scheduler thread. | Yes | Verified via mock assertions |
| Integration | Verify Spring context loads `@EnableScheduling` and configures the `TaskScheduler` bean successfully. | Yes | Spring Boot integration test |

## Risks and Trade-offs

| Risk or Trade-off | Impact | Likelihood | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| Thread hang on browser automation blocks subsequent scheduling cycles | High | Medium | Downstream browser automation actions must enforce strict timeouts (e.g. 30s) so the thread is released and the scheduler can recover. | Open |

## Assumptions

- The hosting system server clock is synchronized (e.g., via NTP) and accurately reflects the real time.

## Open Questions

| Question | Impact | Blocks Create Tasks? | Suggested Owner |
| --- | --- | --- | --- |
| None | None | No | None |

## Feature Technical Readiness

Status: Ready for Task Breakdown

Reason: The scheduling engine architecture, timezone management, operational window verification, error boundaries, and integration interfaces are fully defined using the project's confirmed technology decisions.

## Feature Technical Readiness Checklist

- [x] Feature scope is clear.
- [x] Product completion criteria are understood.
- [x] Technology decisions are confirmed.
- [x] Applicable guidelines and references are listed.
- [x] Integration contracts are defined or marked as not applicable.
- [x] Data model is defined or marked as not applicable.
- [x] Data contracts are defined or marked as not applicable.
- [x] State and error handling are defined.
- [x] Validation rules are defined or marked as not applicable.
- [x] Security/permission considerations are defined or marked as not applicable.
- [x] Testing strategy is defined.
- [x] Blocking open questions are resolved.
- [x] Inputs for `create-tasks` are clear.

## Inputs for Create Tasks

- Create tasks for scheduling infrastructure configuration (`SchedulerConfig`).
- Create tasks for time/day window filter logic and `ActivityScheduler` implementation.
- Create tasks for scheduling unit tests verifying timezone filtering and error safety.
- Create task for verifying feature completion (`999-verify-feature-completion.md` placeholder).

## ADR Candidates

| Candidate ADR | Decision Area | Status | Reason |
| --- | --- | --- | --- |
| None | None | None | Spring scheduling meets all requirements natively |

## Next Recommended Steps

- Run the `create-tasks` skill to break this specification down into developer tasks under `docs/features/activity-scheduler/tasks/`.
