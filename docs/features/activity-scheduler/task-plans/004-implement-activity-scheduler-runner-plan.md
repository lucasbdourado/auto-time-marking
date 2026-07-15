# Task Implementation Plan: Implement ActivityScheduler Background Runner

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/activity-scheduler/task-plans/004-implement-activity-scheduler-runner-plan.md`

## Task Reference

Task ID: `TSK-AS-004`

Task file: `docs/features/activity-scheduler/tasks/004-implement-activity-scheduler-runner.md`

Task status: `Ready`

## Feature Reference

Feature name: `activity-scheduler`

Feature file: `docs/features/activity-scheduler/feature.md`

Feature Tech Spec: `docs/features/activity-scheduler/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

List every required document, optional document, guideline, decision, localized codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/activity-scheduler/tasks/004-implement-activity-scheduler-runner.md` | Goal, Scope, AC | Confirmed by source document | Defines the requirements of the activity scheduler runner. |
| Feature file | `docs/features/activity-scheduler/feature.md` | Goal, Scope | Confirmed by source document | Defines the overall feature goal and scope. |
| Feature Tech Spec | `docs/features/activity-scheduler/tech-spec.md` | Proposed Technical Approach (1 & 4), Modules and Responsibilities, Observability | Confirmed by source document | Defines package, scheduling annotation, error isolation, and logging. |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions | Confirmed by source document | Confirmed stack choices (Java 21, Spring Boot, Spring Scheduling, SLF4J). |

## Planning Scope

The scope of this planning session is limited to designing the `ActivityScheduler` runner class, mapping its dependencies and properties injection, and detailing its scheduling and error recovery mechanism. This plan does not authorize code changes or execution.

## Task Summary

Implement the `ActivityScheduler` class which periodically runs (every 30 minutes by default), queries `SchedulerTimezoneFilter` to check the timezone-aware operating window, triggers the `MarkingWorkflow` execution cycle if inside the window, and logs execution results or skips safely.

## Execution Eligibility

Status: Eligible

Reason:

- All dependencies (TSK-AS-001 and TSK-AS-003) have been successfully implemented in the codebase.

## Feature Context

The activity-scheduler feature automates periodic checking and submission of time markings. The scheduler acts as the loop driver, coordinating the timezone filter checks and calling the marking execution workflow.

## Tech Spec Coverage

Explain how the feature Tech Spec covers this task.

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach -> 1. Scheduler Thread Configuration | Full | Try-catch safety, custom TaskScheduler injection | The TaskScheduler pool is configured by SchedulerConfig. |
| Proposed Technical Approach -> 4. Scheduler Failure Safety | Full | Try-catch isolation and SLF4J error logging | Ensures loop continues even when workflow fails. |
| Modules and Responsibilities -> ActivityScheduler | Full | ActivityScheduler class execution logic | Periodic scheduling via fixedDelayString. |
| State and Error Handling | Full | Execution, Skip, and Workflow Error handling | Map state rules into scheduler execution logic. |

Coverage assessment:

- Justifying Tech Spec section: Proposed Technical Approach (1 & 4) & State and Error Handling.
- Tech Spec sections implemented by this task: Scheduler execution loop, timezone boundary filtering validation, safe error boundaries, and logging.
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

List confirmed technology decisions that constrain this plan.

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 | `technology-definition.md` | Enables modern Java language constructs and time APIs (`java.time`). |
| Spring Task Scheduling | `technology-definition.md` | Constrains scheduling annotations to Spring's `@Scheduled` model. |
| SLF4J + Logback | `technology-definition.md` | Restricts logging to SLF4J facade using standard logging levels. |

## Applicable Guidelines

Record the internal guidelines consulted for this task.

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Camada de Infraestrutura | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Infrastructure components | The scheduler is an infrastructure adapter/component (depends on Spring framework details). |
| Estrutura de Pacotes | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Package boundaries | Defines module folder structures and package layout. |

## Existing Decisions Reviewed

Record documented decisions consulted while preparing the plan.

| Decision | Path | Relevance |
| --- | --- | --- |
| `SchedulerConfig` thread pool configuration | `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfig.java` | Confirms thread pool size 1 and prefix `activity-scheduler-`. |
| `BmaquiosqueProperties` timezone property | `src/main/java/.../BmaquiosqueProperties.java` | Confirms timezone configuration field and America/Sao_Paulo default. |

## Local Codebase References

Record only localized codebase checks directly related to this task.

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfig.java` | Configuration | Configures the TaskScheduler thread pool. | Needs a new bean declaration for `SchedulerTimezoneFilter`. |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/MarkingWorkflow.java` | Interface | The interface to execute the marking cycle. | Will be constructor-injected. |

## Confirmed Scope

List the work confirmed to be part of this task.

- Create package `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling`.
- Create `@Component` class `ActivityScheduler` inside that package.
- Inject `MarkingWorkflow`, `SchedulerTimezoneFilter` (the timezone filter component from TSK-AS-003), the timezone property `bmaquiosque.timezone`, and the scheduler interval property `bmaquiosque.scheduler.interval-ms` (with default 1800000).
- Expose a method annotated with `@Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")`.
- The method must:
  - Obtain current time in the configured timezone.
  - Call `SchedulerTimezoneFilter.isWithinOperatingWindow(ZonedDateTime)` to check if the window is open.
  - If inside window: log cycle start at INFO level and call `MarkingWorkflow.executeMarkingCycle()`.
  - If outside window: log a skip message indicating the current time at INFO level.
  - Intercept all exceptions in a `try-catch` block inside the loop, logging details at `ERROR` level using SLF4J.
- Register `SchedulerTimezoneFilter` as a `@Bean` in `SchedulerConfig` to make it injectable.

## Out of Scope

List related work that must not be done in this task.

- Implementing `SchedulerTimezoneFilter` logic (covered in Task 003).
- Implementing tests (covered in Tasks 005 and 006).
- Creating or configuring the thread pool config (covered in Task 002).
- Implementing `MarkingWorkflow` (covered in downstream features).

## Proposed Implementation Approach

Describe the future implementation approach using only confirmed information.

1. Register `SchedulerTimezoneFilter` in `SchedulerConfig.java` as a `@Bean` so Spring can autowire it.
2. Create `ActivityScheduler.java` under `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling`.
3. Annotate `ActivityScheduler` with `@Component`.
4. Define constructor injection for `MarkingWorkflow`, `SchedulerTimezoneFilter`, and `@Value` properties.
5. Annotate execution method with `@Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")`.
6. Implement `try-catch` wrapper inside the scheduled method to guarantee exception safety.

## Expected Files or Areas

List expected files, modules, packages, docs, tests, or areas. Use probable language when exact paths were not confirmed.

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java` | Create | Confirmed | Task scope | Periodic loop runner component. |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfig.java` | Modify | Confirmed | Injectable beans setup | Modify to add bean method for domain filter class. |

## Implementation Steps

Give the future `execute-task` agent a focused sequence of implementation steps. Do not include executable steps when the plan is blocked by a required ADR or architecture/global decision.

1. Open `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfig.java` and declare a `@Bean` method for `SchedulerTimezoneFilter` returning a new instance of it.
2. Create directory `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/` if it does not exist.
3. Create `ActivityScheduler.java` inside this directory.
4. Implement the class template:
   - Package: `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling`.
   - Imports for SLF4J Logger, org.springframework.beans.factory.annotation.Value, org.springframework.scheduling.annotation.Scheduled, org.springframework.stereotype.Component, java.time.ZonedDateTime, java.time.ZoneId, com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow, com.lucasbdourado.autotimemarking.modules.scheduler.domain.SchedulerTimezoneFilter.
   - Class annotation `@Component`.
   - Fields: `Logger`, `MarkingWorkflow`, `SchedulerTimezoneFilter`, `String timezone`.
   - Constructor injecting these fields, using `@Value("${bmaquiosque.timezone}")` for the timezone field.
   - Scheduled method annotated with `@Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")`.
   - Method logic checks `timezoneFilter.isWithinOperatingWindow(ZonedDateTime.now(ZoneId.of(timezone)))`.
   - Method try-catch block wrapping the check and execution, logging any caught exception at `ERROR` level.
5. Verify compile success via `mvn clean compile`.

## Acceptance Criteria Mapping

Map task acceptance criteria to planned implementation and validation evidence.

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Class `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling.ActivityScheduler` implemented | Yes, class will be created in this location | Inspected code structure. |
| Uses `@Scheduled` with `fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}"` | Yes, method annotated with correct delay string | Inspected code structure. |
| Skips executing `MarkingWorkflow` when window is closed | Yes, if filter returns false, log skip and do not call workflow | Inspected code structure. |
| Invokes `MarkingWorkflow` when window is open | Yes, if filter returns true, invoke workflow | Inspected code structure. |
| Exceptions caught and logged as ERROR | Yes, try-catch block intercepts Exception and logs at error level | Inspected code structure. |
| Correct properties used | Yes, timezone and interval-ms injected via `@Value` | Inspected code structure. |

## Tests and Validation Strategy

Define how the future implementation should be verified.

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Code compilation | Compilation | Verify compile succeeds | `mvn clean compile` |
| Scheduler Runner Unit Tests | Unit | Covered by Task 005 (mocking timezone filter and marking workflow responses) | Defer to Task 005 |
| Scheduler Runner Integration Tests | Integration | Covered by Task 006 (verifying scheduled execution under Spring Boot) | Defer to Task 006 |

## Dependencies

List task dependencies, sequencing constraints, external dependencies, and execution eligibility constraints.

- `001-define-marking-workflow-interface.md` (Implemented)
- `003-implement-scheduler-timezone-filter.md` (Implemented)
- Sequencing constraint: All prerequisite tasks are completed.

## Risks and Edge Cases

List known risks, constraints, regression areas, and edge cases.

- Exception propagating out of scheduler thread: Mitigated by catch-all `Exception` block.
- Timezone validation failure: Call to `ZoneId.of(timezone)` will throw `ZoneRulesException` if the timezone is invalid. Spring Boot validation in `BmaquiosquePropertiesValidator` already validates that the timezone is a valid zone ID, but we should handle it gracefully or let it propagate on startup/execution.

## Rollback or Recovery Notes

Describe rollback, recovery, or safe reversal considerations when relevant.

- git checkout the modified `SchedulerConfig.java` file.
- Delete the created `ActivityScheduler.java` file.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Decisions Created During Planning

No local feature/task decisions were created during this planning session.

## Task Planning Readiness Checklist

- [x] Task file reviewed.
- [x] Feature context reviewed.
- [x] Feature Tech Spec coverage verified.
- [x] Technology decisions reviewed.
- [x] Applicable guidelines reviewed.
- [x] Existing decisions reviewed.
- [x] Local codebase references checked when applicable.
- [x] Task dependencies checked.
- [x] Execution eligibility documented.
- [x] Blocking decisions resolved.
- [x] Local feature/task decisions documented when needed.
- [x] Architecture/global decisions routed to ADR or `resolve-architecture-blocker` when needed.
- [x] Implementation approach defined.
- [x] Acceptance criteria mapped.
- [x] Tests and validation strategy defined.
- [x] Risks and rollback notes documented.

## Notes for Execute Task

Add concise handoff notes, source-reading reminders, sequencing constraints, and things the future `execute-task` agent must not assume.

- The class `ActivityScheduler` must catch any `Exception` (including runtime exceptions) to prevent thread death in the `@Scheduled` executor.
- Do not implement unit tests or integration tests in this task.
- Ensure constructor injection is used for all dependencies.
