# Task Implementation Plan: Implement Scheduler Timezone Filter

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/activity-scheduler/task-plans/003-implement-scheduler-timezone-filter-plan.md`

## Task Reference

Task ID: `TSK-AS-003`

Task file: `docs/features/activity-scheduler/tasks/003-implement-scheduler-timezone-filter.md`

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
| Task file | `docs/features/activity-scheduler/tasks/003-implement-scheduler-timezone-filter.md` | Goal, Scope, AC | Confirmed by source document | Defines timezone filter requirements and boundaries. |
| Feature file | `docs/features/activity-scheduler/feature.md` | Goal, Scope | Confirmed by source document | Defines feature goal, operating windows, and days. |
| Feature Tech Spec | `docs/features/activity-scheduler/tech-spec.md` | Proposed Technical Approach (2), Validation Rules, Testing Strategy | Confirmed by source document | Defines timezone filter technical design using java.time API. |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions, Internal Guidelines | Confirmed by source document | Confirmed stack (Java 21, Spring Boot 3.4.x, JUnit 5 + Mockito). |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/README.md` | Camada de Domínio, Princípios gerais | Confirmed by source document | Domain classes must be pure Java and must not depend on Spring or technical details. |

## Planning Scope

The scope of this planning session is limited to designing the timezone-aware operating window filter utility/service class (`SchedulerTimezoneFilter`) and mapping out its exact implementation and verification requirements. This plan does not authorize code changes or scheduling runner implementation.

## Task Summary

Implement a timezone-aware filter utility (`SchedulerTimezoneFilter`) in the domain layer that takes a `ZonedDateTime` and determines whether it lies within the allowed operational window (Monday to Friday, 05:00:00 to 22:00:00 inclusive).

## Execution Eligibility

Status: Eligible

Reason:
- The task does not depend on any previous scheduler module tasks. It can be developed and unit-tested in isolation using mock `ZonedDateTime` values.

## Feature Context

The scheduler runner must only invoke the marking automation workflow when the current time in the configured timezone (`bmaquiosque.timezone`) falls within the business hours (Mon-Fri, 05:00 to 22:00 inclusive). The timezone checking logic needs to be encapsulated in a highly-testable component, isolated from actual system clocks.

## Tech Spec Coverage

Explain how the feature Tech Spec covers this task.

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach -> 2. Timezone-Aware Filter Logic | Full | Encapsulation of window filter rules | Using `java.time` API. |
| Validation Rules -> Day of Week, Time Window | Full | Saturday/Sunday filter, 05:00 to 22:00 time filter | Monday-Friday, 05:00-22:00 inclusive. |
| Testing Strategy -> Unit | Full | Validation strategy definition | Unit tests themselves are deferred to Task 005. |

Coverage assessment:
- Justifying Tech Spec section: Proposed Technical Approach (Section 2) & Validation Rules.
- Tech Spec sections implemented by this task: Day of Week and Time Window validations.
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

List confirmed technology decisions that constrain this plan.

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 | `technology-definition.md` | Enables modern Java language constructs and time APIs (`java.time`). |
| JUnit 5 + Mockito | `technology-definition.md` | Constrains the testing strategy for verifying boundaries. |

## Applicable Guidelines

Record the internal guidelines consulted for this task.

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Camada de Domínio | `.agents/docs/architecture/coding-guidelines/domain-layer.md` | Domain classes | The filter represents a domain rule. It must be a pure Java class without Spring annotations (like `@Component` or `@Service`). |
| Guia de Serviços | `.agents/docs/architecture/coding-guidelines/service-guidelines.md` | Service structure | The timezone checker is a stateless domain utility class. |

## Existing Decisions Reviewed

Record documented decisions consulted while preparing the plan.

| Decision | Path | Relevance |
| --- | --- | --- |
| `bmaquiosque.timezone` property configuration | `src/main/resources/application.properties` | Defines the property setting used by the application caller to determine the target timezone. |

## Local Codebase References

Record only localized codebase checks directly related to this task.

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` | Configuration fields | Verified timezone property field and default value. | Timezone defaults to "America/Sao_Paulo". |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/MarkingWorkflow.java` | Domain package | Confirmed domain package path. | Package is `com.lucasbdourado.autotimemarking.modules.scheduler.domain`. |

## Confirmed Scope

List the work confirmed to be part of this task.

- Create the `SchedulerTimezoneFilter` class in the scheduler domain package.
- Implement the stateless filter method `isWithinOperatingWindow(ZonedDateTime zonedDateTime)` that returns a boolean indicating whether the window is open.
- Allowed day of week boundary: Monday to Friday (inclusive).
- Allowed local time boundary: 05:00:00 to 22:00:00 (inclusive).

## Out of Scope

List related work that must not be done in this task.

- Implementing the `@Scheduled` annotation runner logic (Task 004).
- Creating final unit tests for the filter class (Task 005).
- Configuring the Spring Scheduler thread pool (Task 002).

## Proposed Implementation Approach

1. Create a plain Java class `SchedulerTimezoneFilter` under `com.lucasbdourado.autotimemarking.modules.scheduler.domain`.
2. Define static/final boundary fields `START_TIME` (05:00) and `END_TIME` (22:00) using `java.time.LocalTime`.
3. Implement `public boolean isWithinOperatingWindow(ZonedDateTime zonedDateTime)`:
   - Perform null check on the input.
   - Extract the Day of Week and verify it is not `SATURDAY` or `SUNDAY`.
   - Extract the Local Time and check that it is not before `START_TIME` and not after `END_TIME`.
4. The caller (Task 004 runner) will load the timezone configuration property, construct the target `ZonedDateTime`, and query this filter.

## Expected Files or Areas

List expected files, modules, packages, docs, tests, or areas.

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilter.java` | Create | Confirmed | Task goal | Pure Java domain class. |

## Implementation Steps

1. Create the file `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilter.java`.
2. Implement class package declaration: `package com.lucasbdourado.autotimemarking.modules.scheduler.domain;`.
3. Declare `SchedulerTimezoneFilter` class.
4. Implement `START_TIME` as `LocalTime.of(5, 0, 0)` and `END_TIME` as `LocalTime.of(22, 0, 0)`.
5. Implement `public boolean isWithinOperatingWindow(ZonedDateTime zonedDateTime)` containing:
   - Null safety return.
   - Day of week checks.
   - Boundary comparison logic.
6. Verify compilation by running `mvn clean compile`.

## Acceptance Criteria Mapping

Map task acceptance criteria to planned implementation and validation evidence.

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Timezone check logic correct using `java.time` | Timezone-aware logic will accept a `ZonedDateTime` parameter. | Inspected code structure. |
| Saturday/Sunday identified as outside | If day is `SATURDAY` or `SUNDAY`, return `false`. | Inspected code structure. |
| Time window boundary checks (05:00:00 inside, 04:59:59 outside, 22:00:00 inside, 22:00:01 outside) | Boundary comparisons using `LocalTime.isBefore` and `LocalTime.isAfter`. | Inspected code structure. |
| System clock can be mocked / bypassed | Handled because the method accepts a reference `ZonedDateTime` directly. | Inspected method signature. |

## Tests and Validation Strategy

Define how the future implementation should be verified.

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Code compilation | Compilation | Verify class structure, imports, and syntax. | Run `mvn clean compile`. |
| Boundary Unit Tests (Task 005) | Unit | Comprehensive verification of all edge conditions (weekday boundaries, time boundaries). | Defer actual unit test creation to Task 005. |

## Dependencies

- None. (Task TSK-AS-003 is independent and does not require Task 001 or Task 002 to compile or execute).

## Risks and Edge Cases

- **Timezone mismatch**: Ensure that callers construct `ZonedDateTime` using the configured timezone (`ZoneId.of(timezone)`) rather than default system timezone.
- **Null Inputs**: If `zonedDateTime` passed is null, return `false` gracefully.

## Rollback or Recovery Notes

- Rollback can be performed via Git command: `git checkout -- src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilter.java` or deletion of the created file.

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

- The class `SchedulerTimezoneFilter` must remain a pure Java class without Spring annotations, conforming to domain layer guidelines.
- The unit tests covering the boundary cases are part of Task 005, not this task.
