# Task: Implement ActivityScheduler Background Runner

## Status

Depends on Previous Task

## Task ID

TSK-AS-004

## Feature

`docs/features/activity-scheduler/feature.md`

## Source Documents

- `docs/features/activity-scheduler/feature.md`
- `docs/features/activity-scheduler/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `ActivityScheduler` class which executes periodically, verifies timezone-aware time filters, triggers the `MarkingWorkflow` execution cycle, and logs execution signals safely.

## Context

The main entry point of the background runner must run at a 30-minute interval configured via property `bmaquiosque.scheduler.interval-ms`. It must load the configured timezone `bmaquiosque.timezone`, check if the current time matches the operational window, run the marking workflow, and intercept any thrown exception to log it and ensure the thread loop continues.

## Scope

- Create package `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling`.
- Create `@Component` class `ActivityScheduler`.
- Inject `MarkingWorkflow`, the timezone filter component, and properties `bmaquiosque.timezone` and `bmaquiosque.scheduler.interval-ms`.
- Expose a method annotated with `@Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")`.
- The method must:
  - Get the current `ZonedDateTime` in the configured timezone.
  - Call the timezone filter logic to check if the window is open.
  - If inside window: log start, and invoke `MarkingWorkflow.executeMarkingCycle()`.
  - If outside window: log a skip message indicating the current time and day.
  - Intercept all exceptions in a `try-catch` block inside the loop, logging details at `ERROR` level using SLF4J logger.

## Out of Scope

- Implementing the tests (covered in Tasks 005 and 006).
- Creating or configuring the thread pool config (covered in Task 002).

## Depends On

- `001-define-marking-workflow-interface.md`
- `003-implement-scheduler-timezone-filter.md`

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- The class `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling.ActivityScheduler` is implemented.
- It uses `@Scheduled` with `fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}"` to schedule the task.
- It skips executing `MarkingWorkflow` when the timezone filter determines the window is closed.
- It invokes `MarkingWorkflow` when the window is open.
- Any exception thrown by `MarkingWorkflow.executeMarkingCycle()` is caught and logged as `ERROR` without throwing out of the scheduled method.
- Correct properties `bmaquiosque.timezone` and `bmaquiosque.scheduler.interval-ms` are injected/used.

## Implementation Notes

- Follow the confirmed stack and constraints from `docs/architecture/auto-time-marking/technology-definition.md`.
- Keep the work limited to this feature and task scope.
- Do not introduce new architecture, libraries, persistence, API contracts, or product behavior unless already defined in the source documents.
- If implementation requires an undocumented decision, keep the task blocked or defer the decision to `plan-task`.

## Validation Notes

- Run `mvn clean compile` to verify compilation.

## Risks

- Overlapping schedule runs: Avoided by using `@Scheduled(fixedDelayString = ...)` instead of `fixedRate`.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
