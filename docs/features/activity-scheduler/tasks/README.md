# Task Breakdown: activity-scheduler

## Status

Confirmed

## Product Name

Auto Time Marking

## Feature Reference

`docs/features/activity-scheduler/feature.md`

## Source Documents

- `docs/features/activity-scheduler/feature.md`
- `docs/features/activity-scheduler/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Task Strategy

The activity-scheduler feature has been decomposed into modular tasks following the Clean Architecture and modular structure principles:
1. Define the core domain interface (`MarkingWorkflow`) to establish the contract with downstream automation modules.
2. Set up infrastructure configuration (`SchedulerConfig`) to configure a dedicated, isolated task scheduler thread pool.
3. Build the timezone-aware execution filter component to encapsulate time checking logic, ensuring high unit testability without relying on the system clock.
4. Implement the background scheduling runner (`ActivityScheduler`) leveraging Spring Boot `@Scheduled` annotation to orchestrate the scheduling flow.
5. Create comprehensive unit and integration tests to verify both behavior boundaries and correct container configuration.
6. Verify feature completion via a final end-to-end check task.

## Task List

| Order | Task File | Goal | Status | Depends On | Blocking Reason |
|---|---|---|---|---|---|
| 001 | `001-define-marking-workflow-interface.md` | Define the MarkingWorkflow domain interface. | Ready | None | None |
| 002 | `002-configure-scheduler-thread-pool.md` | Configure the custom TaskScheduler thread pool in SchedulerConfig. | Ready | None | None |
| 003 | `003-implement-scheduler-timezone-filter.md` | Implement the timezone-aware window check logic. | Ready | None | None |
| 004 | `004-implement-activity-scheduler-runner.md` | Implement the ActivityScheduler component with @Scheduled. | Depends on Previous Task | `001-define-marking-workflow-interface.md`, `003-implement-scheduler-timezone-filter.md` | None |
| 005 | `005-write-scheduler-unit-tests.md` | Implement unit tests for window filtering and failure recovery. | Depends on Previous Task | `004-implement-activity-scheduler-runner.md` | None |
| 006 | `006-write-scheduler-integration-tests.md` | Implement integration tests for Spring configuration. | Depends on Previous Task | `002-configure-scheduler-thread-pool.md` | None |
| 999 | `999-verify-feature-completion.md` | Validate feature completion and criteria verification. | Depends on Previous Task | `005-write-scheduler-unit-tests.md`, `006-write-scheduler-integration-tests.md` | None |

## Suggested Execution Order

1. `001-define-marking-workflow-interface.md` (Domain contract setup)
2. `002-configure-scheduler-thread-pool.md` (Scheduler infra setup)
3. `003-implement-scheduler-timezone-filter.md` (Window logic utility setup)
4. `004-implement-activity-scheduler-runner.md` (Scheduler runner orchestration)
5. `005-write-scheduler-unit-tests.md` (Unit test validations)
6. `006-write-scheduler-integration-tests.md` (Integration test validation)
7. `999-verify-feature-completion.md` (Overall verification)

## Blocked Tasks

| Task File | Blocking Reason | Required Action |
|---|---|---|
| None | None | None |

## Dependency Notes

- Task `004` directly orchestrates the window checking and work execution, therefore it requires the domain interface (`001`) and the filter logic (`003`) to be implemented first.
- Task `005` relies on the complete implementation of `004` to perform unit tests on it.
- Task `006` checks the thread pool configuration established in `002`.
- Task `999` is the final step checking that all other tasks have been executed and the feature is fully complete.

## Notes for Plan Task

- Plan one task at a time.
- Read the task file and its source documents before creating a task implementation plan.
- Do not plan blocked tasks until their blocking reason is resolved.

## Notes for Execute Task

- Execute only from an approved task implementation plan.
- Validate each task against its acceptance criteria.
- Do not mark the feature complete until `999-verify-feature-completion.md` is satisfied.
