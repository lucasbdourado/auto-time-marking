# Task: Implement Scheduler Timezone Filter

## Status

Ready

## Task ID

TSK-AS-003

## Feature

`docs/features/activity-scheduler/feature.md`

## Source Documents

- `docs/features/activity-scheduler/feature.md`
- `docs/features/activity-scheduler/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement a timezone-aware filter utility/service that determines whether a given date-time lies within the allowed operational window (Monday to Friday, 05:00 to 22:00 inclusive) for the configured timezone.

## Context

The automation loop must only fire when it is between Monday and Friday, and between 05:00:00 and 22:00:00 inclusive, in the timezone specified by configuration `bmaquiosque.timezone`. To ensure high testability, this logic should be encapsulated in a class or component that can be tested under simulated times.

## Scope

- Create a timezone checker component (e.g. `SchedulerFilter` or similar class).
- Implement a method that accepts a timezone identifier and a reference time/clock (or takes a `ZonedDateTime`) and returns a boolean indicating whether the window is open.
- Allowed day of week boundary: Monday to Friday (inclusive).
- Allowed local time boundary: 05:00:00 to 22:00:00 (inclusive).

## Out of Scope

- Implementing the `@Scheduled` runner logic itself.
- Triggering browser automation.

## Depends On

None

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- The timezone check logic is correctly implemented using the `java.time` API.
- Day of week check correctly identifies Saturday and Sunday as outside window.
- Time window check correctly identifies boundaries:
  - Exact start: 05:00:00 (inside)
  - Pre-start: 04:59:59 (outside)
  - Exact end: 22:00:00 (inside)
  - Post-end: 22:00:01 (outside)
- System clock can be mocked or bypassed in tests to verify different times.

## Implementation Notes

- Follow the confirmed stack and constraints from `docs/architecture/auto-time-marking/technology-definition.md`.
- Keep the work limited to this feature and task scope.
- Do not introduce new architecture, libraries, persistence, API contracts, or product behavior unless already defined in the source documents.
- If implementation requires an undocumented decision, keep the task blocked or defer the decision to `plan-task`.

## Validation Notes

- Code compiles successfully.
- Tests covering all edge conditions will be implemented in Task 005.

## Risks

- Timezone conversions can result in off-by-one day bugs if local dates are not properly extracted. Ensure standard timezone conversion via `ZonedDateTime` is used.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
