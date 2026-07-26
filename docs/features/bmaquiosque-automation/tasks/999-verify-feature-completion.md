# Task: Verify Feature Completion

## Status

Depends on Previous Task

## Task ID

TSK-BMA-999

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Perform final E2E verification of the `bmaquiosque-automation` feature, confirming all product and technical acceptance criteria are fully met.

## Context

Before marking the feature as complete, we must execute a structured review checklist to ensure code quality, test coverage, logging behavior, and lack of credential leakage.

## Scope

- Verify all sub-tasks (TSK-BMA-001 through TSK-BMA-007) are marked as `Done` or `Implemented`.
- Execute a clean build: `mvn clean test` and verify all tests pass.
- Verify logging output does not print raw passwords/credentials during browser startup, navigation, or failure modes.
- Verify screenshots are generated inside `logs/screenshots/` when an invalid selector is configured and execution fails.
- Verify that `RetryingTimeClockClient` is injected by default when autowiring `TimeClockClient` in other modules.

## Out of Scope

- Performing verification of other features (such as scheduling calculations or audit logging).

## Depends On

- 001-add-playwright-dependency.md
- 002-extend-bmaquiosque-properties-with-selectors.md
- 003-update-properties-validator-and-tests.md
- 004-implement-timeclock-client-interface.md
- 005-implement-playwright-timeclock-client.md
- 006-implement-retrying-timeclock-client-decorator.md
- 007-implement-automation-tests.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- All tasks are completed.
- The build succeeds and all automated tests pass.
- No sensitive credentials are leaked in any log files or CLI console.
- Screenshots are successfully saved during simulated errors.

## Implementation Notes

- Verify that the screenshot folder `logs/screenshots` exists and is ignored in `.gitignore` if needed (ensure we do not commit local debug screenshots).

## Validation Notes

- Run `mvn clean test` as the final automated check.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
