# Task Implementation Plan: Write Scheduler Integration Tests

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/activity-scheduler/task-plans/006-write-scheduler-integration-tests-plan.md`

## Task Reference

Task ID: `TSK-AS-006`

Task file: `docs/features/activity-scheduler/tasks/006-write-scheduler-integration-tests.md`

Task status: `Depends on Previous Task`

## Feature Reference

Feature name: `activity-scheduler`

Feature file: `docs/features/activity-scheduler/feature.md`

Feature Tech Spec: `docs/features/activity-scheduler/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

List every required document, optional document, guideline, decision, localized codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/activity-scheduler/tasks/006-write-scheduler-integration-tests.md` | Entire file | Confirmed by source document | Defines the integration test requirements and scope. |
| Feature file | `docs/features/activity-scheduler/feature.md` | Entire file | Confirmed by source document | Provides product context for background scheduling. |
| Feature Tech Spec | `docs/features/activity-scheduler/tech-spec.md` | Section 19. Testing Strategy | Confirmed by source document | Mandates a Spring Boot integration test to verify scheduling config. |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Section 10. Confirmed Technology Decisions | Confirmed by source document | Defines framework (Spring Boot 3.4.x) and test dependencies. |

## Planning Scope

Explain the exact boundary of this planning session. This plan covers one task only and does not authorize implementation.

This planning session covers only the design and validation steps for Task `TSK-AS-006` (Write Scheduler Integration Tests) in the `activity-scheduler` feature. It does not authorize writing or editing source or test code.

## Task Summary

Summarize the single concrete outcome this task must produce.

Create a Spring Boot integration test class `SchedulerConfigIntegrationTest` annotated with `@SpringBootTest` to verify that the application context loads successfully with scheduling enabled, and the custom `TaskScheduler` bean is correctly configured with a pool size of 1 and a thread name prefix starting with `activity-scheduler-`.

## Execution Eligibility

Status: Eligible

Reason:

- The only dependency of this task is `002-configure-scheduler-thread-pool.md`, which is already marked as `Implemented` and its implementation verified in `SchedulerConfig.java`. Therefore, this task is eligible for execution.

## Feature Context

Summarize only the feature context needed to understand why this task exists and how it fits the feature.

The `activity-scheduler` feature runs background task checks every 30 minutes in a configured timezone. Setting up an isolated, dedicated scheduler thread pool of size 1 with a specific name prefix ensures predictable scheduling execution. The integration test verifies that Spring Boot initializes and configures this pool as expected.

## Tech Spec Coverage

Explain how the feature Tech Spec covers this task.

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| 19. Testing Strategy | Full | Spring Boot integration test verifying `@EnableScheduling` and `TaskScheduler` configuration | None |
| 8. Proposed Technical Approach -> 1. Scheduler Thread Configuration | Full | Verification of thread pool size and thread name prefix | None |

Coverage assessment:

- Justifying Tech Spec section: `19. Testing Strategy`
- Tech Spec sections implemented by this task: `19. Testing Strategy` and `8. Proposed Technical Approach -> 1. Scheduler Thread Configuration`
- Gaps between task and Tech Spec: None
- Dependencies not specified by the Tech Spec: None

## Technology Decisions Used

List confirmed technology decisions that constrain this plan.

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Spring Boot 3.4.x | `technology-definition.md` | Requires integration tests to use `@SpringBootTest` and modern testing tools (e.g. `@MockitoBean`). |
| JUnit 5, Spring Boot Test, AssertJ, Mockito | `technology-definition.md` | Used as the standard assertion and testing libraries in the project. |

## Applicable Guidelines

Record the internal guidelines consulted for this task.

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Java / Clean Architecture Guidelines | `.agents/docs/architecture/coding-guidelines/README.md` | Whole project | Guides directory layout, package path conventions, and test structure. |

## Existing Decisions Reviewed

Record documented decisions consulted while preparing the plan.

| Decision | Path | Relevance |
| --- | --- | --- |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed stack and testing framework choices. |

## Local Codebase References

Record only localized codebase checks directly related to this task.

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfig.java` | Confirms definition of `@EnableScheduling` and `TaskScheduler` bean | The test targets this configuration class. | Verifies package structure and bean names. |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java` | Identifies dependencies (`MarkingWorkflow`, `SchedulerTimezoneFilter`) | Used to determine what mock beans are required in the test context. | Verifies constructor parameters. |

## Confirmed Scope

List the work confirmed to be part of this task.

- Create the directory `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/` if it does not exist.
- Implement `SchedulerConfigIntegrationTest.java` annotated with `@SpringBootTest`.
- Supply valid dummy properties to `@SpringBootTest(properties = { ... })` to satisfy startup configuration validations.
- Mock `MarkingWorkflow` using `@MockitoBean` because no concrete implementation class exists yet.
- Inject the `TaskScheduler` bean and verify it is not null, is an instance of `ThreadPoolTaskScheduler`, has a pool size of 1, and has a thread name prefix starting with `activity-scheduler-`.
- Validate that the Spring application context starts up without errors.

## Out of Scope

List related work that must not be done in this task.

- Testing actual periodic run intervals or execution frequencies.
- Testing browser automation or time-clock marking execution flow.
- Writing unit tests for timezone filtering or error safety (covered by task 005).

## Proposed Implementation Approach

Describe the future implementation approach using only confirmed information.

1. Annotate the test class with `@SpringBootTest` and supply valid configuration properties to satisfy configuration startup validations.
2. Mock the `MarkingWorkflow` interface since it has no implementation yet.
3. Autowire the `TaskScheduler` bean.
4. Assert that the bean is of class `ThreadPoolTaskScheduler`.
5. Check the configured pool size and thread name prefix.

## Expected Files or Areas

List expected files, modules, packages, docs, tests, or areas. Use probable language when exact paths were not confirmed.

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfigIntegrationTest.java` | Create | Confirmed | Task description | Target integration test file. |

## Implementation Steps

Give the future `execute-task` agent a focused sequence of implementation steps. Do not include executable steps when the plan is blocked by a required ADR or architecture/global decision.

1. Create package directory `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/` if it does not exist.
2. Create test class `SchedulerConfigIntegrationTest.java` in that package.
3. Annotate the class with `@SpringBootTest(properties = { ... })` specifying valid dummy values for:
   - `bmaquiosque.username=test-user`
   - `bmaquiosque.password=test-password`
   - `bmaquiosque.max-entry-time=09:00`
   - `bmaquiosque.jitter-minutes=5`
   - `bmaquiosque.timezone=America/Sao_Paulo`
4. Declare a mock bean of type `MarkingWorkflow` using `@MockitoBean`.
5. Autowire `TaskScheduler taskScheduler`.
6. Add a test method annotated with `@Test` (e.g., `shouldConfigureTaskSchedulerCorrectly`).
7. Use AssertJ to assert that `taskScheduler` is not null and is an instance of `ThreadPoolTaskScheduler`.
8. Cast to `ThreadPoolTaskScheduler` and assert that `getPoolSize()` is `1` and `getThreadNamePrefix()` is `"activity-scheduler-"`.
9. Run `mvn test` to verify the test runs and passes successfully.

## Acceptance Criteria Mapping

Map task acceptance criteria to planned implementation and validation evidence.

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Integration test class is created in the test directories | Created under `src/test/java/.../infrastructure/config/` | `SchedulerConfigIntegrationTest.java` file exists. |
| The test loads the full Spring ApplicationContext successfully | `@SpringBootTest` runs and class loads without failure | Test executes successfully. |
| The test asserts the presence of the configured `TaskScheduler` bean | `@Autowired TaskScheduler` injected | `assertThat(taskScheduler).isNotNull()` passes. |
| The test asserts that the pool size and thread prefix match the technical requirements | Cast and assert pool size & prefix | `assertThat(poolScheduler.getPoolSize()).isEqualTo(1)` and `assertThat(poolScheduler.getThreadNamePrefix()).isEqualTo("activity-scheduler-")` pass. |
| Running `mvn test` executes and passes this integration test | Run `mvn test` | Command output shows successful execution of the integration test. |

## Tests and Validation Strategy

Define how the future implementation should be verified.

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `SchedulerConfigIntegrationTest` | Integration | Verify Spring context load and TaskScheduler configuration details | Uses `@SpringBootTest` and AssertJ. |
| `mvn test` execution | Command | Verify all tests pass including the new integration test | Execute in project directory. |

## Dependencies

List task dependencies, sequencing constraints, external dependencies, and execution eligibility constraints.

- Internal: Depends on task `002` configuration being active (already met).

## Risks and Edge Cases

List known risks, constraints, regression areas, and edge cases.

- Context startup failure due to missing validation properties: Mitigated by providing valid dummy properties via `@SpringBootTest(properties = ...)`.
- Context startup failure due to missing `MarkingWorkflow` implementation: Mitigated by mock bean definition using `@MockitoBean`.

## Rollback or Recovery Notes

Describe rollback, recovery, or safe reversal considerations when relevant.

- If the test setup fails or requires modification, delete the `SchedulerConfigIntegrationTest.java` file or revert git changes.

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

- Remember to mock the `MarkingWorkflow` bean since there is no concrete class implementing it yet.
- Ensure the Spring Boot 3.4.1 annotation `@MockitoBean` is used for mocking Spring beans, rather than `@MockBean`.
- Ensure that valid dummy properties are provided in the `@SpringBootTest` annotation to satisfy the validator startup hook checks.
