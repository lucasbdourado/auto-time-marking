# Task: Write Scheduler Unit Tests

## Status

Implemented

## Task ID

TSK-AS-005

## Feature

`docs/features/activity-scheduler/feature.md`

## Source Documents

- `docs/features/activity-scheduler/feature.md`
- `docs/features/activity-scheduler/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Write JUnit 5 unit tests with Mockito to validate timezone-aware filtering under boundary conditions, and verify exception interception/safety in the scheduler runner.

## Context

The scheduling logic must be fully tested to guarantee time-range filters block execution correctly at the boundaries, and that exceptions thrown by the marking workflow do not kill the scheduler thread.

## Scope

- Create a test class for the timezone filter logic.
- Cover the following scenarios using a mocked or simulated clock/time:
  - Monday 04:59 (Skip)
  - Monday 05:00 (Run)
  - Monday 22:00 (Run)
  - Monday 22:01 (Skip)
  - Saturday (any time - Skip)
  - Sunday (any time - Skip)
  - Wednesday 12:00 (Run)
- Create a test class for `ActivityScheduler` to:
  - Mock `MarkingWorkflow` and verify it is called when the filter says "inside window".
  - Mock `MarkingWorkflow` to throw an exception, and assert that the exception is caught by `ActivityScheduler` and logged, rather than propagated.
  - Verify `MarkingWorkflow` is NOT called when the filter says "outside window".

## Out of Scope

- Performing full Spring Boot integration tests (covered by Task 006).

## Depends On

- `004-implement-activity-scheduler-runner.md`

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- Test classes are created under `src/test/java`.
- Tests run and pass using JUnit 5 and Mockito.
- Tests verify all boundary conditions of day-of-week and time-window filtering.
- Tests verify that exceptions thrown in the workflow do not escape the scheduler runner's scheduled method.

## Implementation Notes

- Follow the confirmed stack and constraints from `docs/architecture/auto-time-marking/technology-definition.md`.
- Keep the work limited to this feature and task scope.
- Do not introduce new architecture, libraries, persistence, API contracts, or product behavior unless already defined in the source documents.
- If implementation requires an undocumented decision, keep the task blocked or defer the decision to `plan-task`.

## Validation Notes

- Run `mvn clean test` to execute unit tests.

## Risks

- Flaky tests if tests depend on actual system time. Ensure all time checks are driven by a mockable Clock, Mockito, or test datetime values.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
