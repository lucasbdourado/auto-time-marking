# Task Implementation Plan: Write Scheduler Unit Tests

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/activity-scheduler/task-plans/005-write-scheduler-unit-tests-plan.md`

## Task Reference

Task ID: `TSK-AS-005`

Task file: `docs/features/activity-scheduler/tasks/005-write-scheduler-unit-tests.md`

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
| Task file | `docs/features/activity-scheduler/tasks/005-write-scheduler-unit-tests.md` | Goal, Scope, AC | Confirmed by source document | Defines required test classes, scenarios, and assertions. |
| Feature file | `docs/features/activity-scheduler/feature.md` | Goal, Scope, Completion Criteria | Confirmed by source document | Defines feature goal, operating windows, and days. |
| Feature Tech Spec | `docs/features/activity-scheduler/tech-spec.md` | Proposed Technical Approach, State and Error Handling, Testing Strategy | Confirmed by source document | Details unit testing strategy, boundary conditions, and mock requirements. |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions, Testing | Confirmed by source document | Confirmed stack choices (Java 21, Spring Boot, JUnit 5 + Mockito). |
| Codebase File | `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilter.java` | Package structure and class methods | Confirmed by codebase check | Source class to be tested. |
| Codebase File | `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/MarkingWorkflow.java` | Interface structure | Confirmed by codebase check | Dependency to be mocked. |
| Codebase File | `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java` | Methods to test | Confirmed by codebase check | Source class to be tested. |
| Codebase File | `src/test/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverterTest.java` | Testing style | Confirmed by codebase check | Reference for JUnit 5, AssertJ, and Mockito styles. |

## Planning Scope

The scope of this planning session is limited to designing the unit tests for `SchedulerTimezoneFilter` and `ActivityScheduler` runner class. This plan covers only the unit testing task (TSK-AS-005) and does not authorize code changes or execution of tests.

## Task Summary

Create and implement two unit test classes (`SchedulerTimezoneFilterTest` and `ActivitySchedulerTest`) under `src/test/java` to comprehensively cover day-of-week and time-window boundaries, verify that the scheduler runner invokes the marking workflow correctly inside the operating window, and assert that exceptions thrown by the workflow do not escape the scheduler runner.

## Execution Eligibility

Status: Eligible

Reason:

- The dependency `004-implement-activity-scheduler-runner.md` (TSK-AS-004) has been implemented, and the `ActivityScheduler` class is fully available in the codebase. Therefore, this task is eligible for execution.

## Feature Context

The scheduler runner must only execute the time-marking cycle when it is within the allowed operating window (Monday to Friday, 05:00:00 to 22:00:00 inclusive) to automate periodic status checks and punch submissions. The unit tests are crucial to ensure that these boundaries are strictly respected and that any runtime exceptions during marking executions are caught cleanly without crashing the scheduler thread.

## Tech Spec Coverage

Explain how the feature Tech Spec covers this task.

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Testing Strategy -> Unit | Full | Validation of filter logic and runner error handling | Explicitly maps to the unit test requirements. |
| Validation Rules | Full | Verifying Day of Week and Time Window boundaries | Tests are designed against these rules. |
| State and Error Handling | Full | Testing try-catch exception safety | Ensures exceptions in the workflow do not kill the scheduler. |

Coverage assessment:
- Justifying Tech Spec section: Testing Strategy -> Unit.
- Tech Spec sections implemented by this task: Day of Week and Time Window validation rule unit tests, and Workflow Error handling safety tests.
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

List confirmed technology decisions that constrain this plan.

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 | `technology-definition.md` | Requires tests to be written in Java 21, allowing use of modern language features. |
| JUnit 5 | `technology-definition.md` | Constrains the test runner to JUnit Jupiter framework. |
| Mockito | `technology-definition.md` | Constrains mocking approach to Mockito library. |
| AssertJ | `technology-definition.md` | Constrains test assertions to AssertJ fluent API (`assertThat`). |

## Applicable Guidelines

Record the internal guidelines consulted for this task.

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/README.md` | Whole project | Standard package naming (`com.lucasbdourado.autotimemarking...`) and modular organization. |

## Existing Decisions Reviewed

Record documented decisions consulted while preparing the plan.

| Decision | Path | Relevance |
| --- | --- | --- |
| Task Plan for `ActivityScheduler` | `docs/features/activity-scheduler/task-plans/004-implement-activity-scheduler-runner-plan.md` | Defines expected package, fields, constructor, and `@Scheduled` method of the runner class to test. |

## Local Codebase References

Record only localized codebase checks directly related to this task.

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java` | Actual implemented class | Verifies method names and parameters | The method is named `execute()` and has no parameters. |
| `src/test/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverterTest.java` | Test structure | Confirms usage of `org.junit.jupiter.api.Test`, AssertJ, and Mockito. | Follows the exact package and class structure. |

## Confirmed Scope

List the work confirmed to be part of this task.

- Create the test class `SchedulerTimezoneFilterTest` in package `com.lucasbdourado.autotimemarking.modules.scheduler.domain`.
- Cover the following scenarios for `SchedulerTimezoneFilter.isWithinOperatingWindow(ZonedDateTime)`:
  - Monday 04:59:00 -> returns `false` (Skip)
  - Monday 05:00:00 -> returns `true` (Run)
  - Wednesday 12:00:00 -> returns `true` (Run)
  - Monday 22:00:00 -> returns `true` (Run)
  - Monday 22:01:00 -> returns `false` (Skip)
  - Saturday 12:00:00 -> returns `false` (Skip)
  - Sunday 12:00:00 -> returns `false` (Skip)
  - Null input -> returns `false` (graceful validation)
- Create the test class `ActivitySchedulerTest` in package `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.scheduling`.
- Cover the following scenarios for `ActivityScheduler.execute()`:
  - When `SchedulerTimezoneFilter.isWithinOperatingWindow` returns `true`, verify `MarkingWorkflow.executeMarkingCycle()` is called exactly once.
  - When `SchedulerTimezoneFilter.isWithinOperatingWindow` returns `false`, verify `MarkingWorkflow.executeMarkingCycle()` is never called.
  - When `SchedulerTimezoneFilter.isWithinOperatingWindow` returns `true` but `MarkingWorkflow.executeMarkingCycle()` throws an exception, verify that the exception is caught, the execution completes safely without throwing, and `assertThatCode` asserts no exception escapes the method.

## Out of Scope

List related work that must not be done in this task.

- Implementing Spring Boot integration tests verifying `@Scheduled` context configuration (covered by Task 006).
- Implementing the scheduler runner class itself (covered by Task 004).
- Implementing the timezone filter utility itself (covered by Task 003).

## Proposed Implementation Approach

Describe the future implementation approach using only confirmed information.

1. **Setup directories**: Verify that `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain` and `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling` exist, and create them if not.
2. **Implement `SchedulerTimezoneFilterTest`**:
   - Write a unit test class containing a series of test cases or a parameterized test mapping `ZonedDateTime` inputs to expected boolean outputs.
   - Use a fixed timezone context like `ZoneId.of("America/Sao_Paulo")` to construct local date-times deterministically.
   - Use AssertJ `assertThat(...)` to verify the outputs.
3. **Implement `ActivitySchedulerTest`**:
   - Write a unit test class with Mockito annotations or manual mock creation:
     - `@Mock private MarkingWorkflow markingWorkflow;`
     - `@Mock private SchedulerTimezoneFilter timezoneFilter;`
   - Inject these mocks into `ActivityScheduler` via its constructor.
   - Implement three `@Test` methods:
     - `execute_whenInsideWindow_shouldExecuteWorkflow()`
     - `execute_whenOutsideWindow_shouldSkipWorkflow()`
     - `execute_whenWorkflowThrowsException_shouldCatchAndLog()`
   - Assert exception suppression using AssertJ's `assertThatCode(...).doesNotThrowAnyException()`.

## Expected Files or Areas

List expected files, modules, packages, docs, tests, or areas. Use probable language when exact paths were not confirmed.

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilterTest.java` | Create | Confirmed | Task scope | Unit tests for filter boundary logic. |
| `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivitySchedulerTest.java` | Create | Confirmed | Task scope | Unit tests for runner scheduled execution logic. |

## Implementation Steps

Give the future `execute-task` agent a focused sequence of implementation steps.

1. Ensure package directories exist:
   - `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain`
   - `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling`
2. Create `SchedulerTimezoneFilterTest.java` under the domain test package.
3. Write test cases covering boundary times (04:59, 05:00, 22:00, 22:01) on weekdays (e.g. Monday/Wednesday) and weekend days (Saturday/Sunday).
4. Verify that null arguments are handled gracefully and return `false`.
5. Create `ActivitySchedulerTest.java` under the scheduling infrastructure test package.
6. Set up Mockito mocks for `MarkingWorkflow` and `SchedulerTimezoneFilter`, and constructor-inject them into `ActivityScheduler`. Pass a valid timezone string (e.g., `"America/Sao_Paulo"`).
7. Implement the test method to mock filter returning `true` and check that workflow `executeMarkingCycle()` is invoked once when `execute()` is called.
8. Implement the test method to mock filter returning `false` and check that workflow `executeMarkingCycle()` is never invoked when `execute()` is called.
9. Implement the test method to mock filter returning `true` and workflow throwing an exception, and verify that the exception is caught (the `execute()` method does not throw).
10. Run `mvn clean test` to execute all unit tests and verify they pass.

## Acceptance Criteria Mapping

Map task acceptance criteria to planned implementation and validation evidence.

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Test classes are created under `src/test/java`. | Yes, test classes will be created under the modular structure `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/`. | Verify file paths in PR / repository. |
| Tests run and pass using JUnit 5 and Mockito. | Yes, tests will use Jupiter `@Test` and `@ExtendWith(MockitoExtension.class)`. | Execution of `mvn clean test` yields green builds. |
| Tests verify all boundary conditions of day-of-week and time-window filtering. | Yes, covered by `SchedulerTimezoneFilterTest` scenarios (04:59, 05:00, 22:00, 22:01, Wednesday, Saturday, Sunday). | Assertions in `SchedulerTimezoneFilterTest`. |
| Tests verify that exceptions thrown in the workflow do not escape the scheduler runner's scheduled method. | Yes, covered by `ActivitySchedulerTest` mock throwing exception. | `assertThatCode(() -> scheduler.execute()).doesNotThrowAnyException()` assertion. |

## Tests and Validation Strategy

Define how the future implementation should be verified.

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Unit test execution | Unit | Runs JUnit 5 test cases for scheduler components | Execute `mvn clean test` |
| Test coverage check | Manual | Confirm all required boundary scenarios are present and passing | Inspect test run report |

## Dependencies

List task dependencies, sequencing constraints, external dependencies, and execution eligibility constraints.

- `004-implement-activity-scheduler-runner.md` (TSK-AS-004) - Must be completed so the runner class `ActivityScheduler` exists and can be tested. (Status: Already Implemented in codebase).

## Risks and Edge Cases

List known risks, constraints, regression areas, and edge cases.

- **System Clock dependence**: Avoid using `ZonedDateTime.now()` inside the tests without control. All tests must use explicit, fixed date-time values constructed with specific dates/times (e.g. `ZonedDateTime.of(...)`) to avoid relying on the host's actual current system clock.

## Rollback or Recovery Notes

Describe rollback, recovery, or safe reversal considerations when relevant.

- Delete the test files created under `src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/`.

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

- The unit tests must not trigger any actual browser actions or configuration properties loading (keep tests pure unit tests using Mockito).
- Use fixed date-times in timezone tests to prevent flaky assertions.
