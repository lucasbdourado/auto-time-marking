# Task: Write Scheduler Integration Tests

## Status

Implemented

## Task ID

TSK-AS-006

## Feature

`docs/features/activity-scheduler/feature.md`

## Source Documents

- `docs/features/activity-scheduler/feature.md`
- `docs/features/activity-scheduler/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Write a Spring Boot integration test to verify that scheduling is enabled, the Spring context loads without errors, and the custom TaskScheduler bean is properly configured.

## Context

We must ensure that our scheduling configuration (`SchedulerConfig`) integrates correctly into the Spring container and overrides/customizes the default task executor with a size of 1 and the specified thread name prefix.

## Scope

- Create a Spring Boot integration test class (e.g. `SchedulerConfigIntegrationTest` or similar) annotated with `@SpringBootTest`.
- Inject the `TaskScheduler` bean from the application context.
- Assert that the `TaskScheduler` bean is an instance of `ThreadPoolTaskScheduler` (or the configured type).
- Verify that the thread pool size is set to 1.
- Verify that the thread name prefix starts with `activity-scheduler-`.

## Out of Scope

- Testing the actual periodic run intervals or browser automation.

## Depends On

- `002-configure-scheduler-thread-pool.md`

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- An integration test class is created in the test directories.
- The test loads the full Spring ApplicationContext successfully.
- The test asserts the presence of the configured `TaskScheduler` bean.
- The test asserts that the pool size and thread prefix match the technical requirements.
- Running `mvn test` executes and passes this integration test.

## Implementation Notes

- Follow the confirmed stack and constraints from `docs/architecture/auto-time-marking/technology-definition.md`.
- Keep the work limited to this feature and task scope.
- Do not introduce new architecture, libraries, persistence, API contracts, or product behavior unless already defined in the source documents.
- If implementation requires an undocumented decision, keep the task blocked or defer the decision to `plan-task`.

## Validation Notes

- Run `mvn test` to execute both unit and integration tests.

## Risks

- Integration tests starting up a database or external APIs could fail. Since the MVP doesn't have a database or external API dependencies configured yet, context loading should be simple and lightweight.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
