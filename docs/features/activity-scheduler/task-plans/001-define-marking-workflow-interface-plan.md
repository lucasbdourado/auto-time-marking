# Task Implementation Plan: Define MarkingWorkflow Interface

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/activity-scheduler/task-plans/001-define-marking-workflow-interface-plan.md`

## Task Reference

Task ID: `TSK-AS-001`

Task file: `docs/features/activity-scheduler/tasks/001-define-marking-workflow-interface.md`

Task status: `Ready`

## Feature Reference

Feature name: `activity-scheduler`

Feature file: `docs/features/activity-scheduler/feature.md`

Feature Tech Spec: `docs/features/activity-scheduler/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/activity-scheduler/tasks/001-define-marking-workflow-interface.md` | Whole document | Confirmed by source document | Primary task source |
| Feature file | `docs/features/activity-scheduler/feature.md` | Scope, Out of Scope | Confirmed by source document | Feature goal definition |
| Feature Tech Spec | `docs/features/activity-scheduler/tech-spec.md` | Proposed Technical Approach, API Design | Confirmed by source document | Defines interface contract |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions, Guidelines | Confirmed by source document | Confirmed stack and guidelines |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/domain-layer.md` | O que não pode existir no domínio | Confirmed by source document | Decoupling domain from Spring |
| Package structure | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Estrutura de Pacotes | Confirmed by source document | Clean architecture packages |

## Planning Scope

This plan covers the creation of the Java package `com.lucasbdourado.autotimemarking.modules.scheduler.domain` and the definition of the `MarkingWorkflow` interface within that package. It does not authorize the implementation of the scheduling configuration, the scheduler loop, the time-filtering logic, or the actual marking automation workflow.

## Task Summary

Create the package `com.lucasbdourado.autotimemarking.modules.scheduler.domain` and define the `MarkingWorkflow` interface with a single method `void executeMarkingCycle() throws Exception;`.

## Execution Eligibility

Status: Eligible

Reason:
- The task status is `Ready` and has no dependencies (`Depends On: None`).

## Feature Context

The `activity-scheduler` feature is responsible for triggering a workday status check and time-marking execution cycle every 30 minutes. To separate scheduling concerns from browser automation and work time calculations, the scheduler triggers execution through a decoupled interface (`MarkingWorkflow`).

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach - 3. Execution Interface | Full | Yes | Defines the decoupling approach |
| API or Interface Design - MarkingWorkflow Interface | Full | Yes | Defines the interface signature |

Coverage assessment:
- Justifying Tech Spec section: proposed technical approach and API design sections.
- Tech Spec sections implemented by this task: MarkingWorkflow interface definition.
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 (LTS) | `technology-definition.md` | Determines the language and compiler version. |
| Maven | `technology-definition.md` | Determines the build tool used to compile the new interface. |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Java Guidelines (Domain) | `.agents/docs/architecture/coding-guidelines/domain-layer.md` | Domain Layer | The interface must be defined in the domain package of the module and contain no Spring annotations (`@Component`, etc.) or external framework dependencies. |
| Package Structure | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Package Layout | Defines the directory naming and nesting conventions for clean architecture modules. |

## Existing Decisions Reviewed

No existing feature, ADR, or architecture decision was relevant to this task.

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration` | Package structure of existing module | Package naming conventions | Confirmed that modules are placed under `com.lucasbdourado.autotimemarking.modules`. |

## Confirmed Scope

- Create package `com.lucasbdourado.autotimemarking.modules.scheduler.domain`.
- Create interface `MarkingWorkflow` in `src/main/java/com/lucasbdourado/autotimemarking.modules.scheduler.domain/MarkingWorkflow.java`.
- Declare public method `void executeMarkingCycle() throws Exception;` with Javadoc.
- Verify compilation using Maven.

## Out of Scope

- Any scheduler loop or thread configuration implementations (`ActivityScheduler`, `SchedulerConfig`).
- Any browser automation code, Playwright setup, or workday calculation logic.

## Proposed Implementation Approach

1. Create the package folder `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain` if not already present.
2. Create the file `MarkingWorkflow.java`.
3. Add the interface declaration and `executeMarkingCycle()` signature, along with a clean Javadoc explaining the method's purpose and its throws clause.
4. Run `mvn clean compile` to ensure the project compiles with no issues.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/MarkingWorkflow.java` | Create | Confirmed | Tech Spec Section 4. "API or Interface Design" | Interface file defining the workflow execution contract. |

## Implementation Steps

1. Create the directories for package `com.lucasbdourado.autotimemarking.modules.scheduler.domain` inside `src/main/java`.
2. Create the file `MarkingWorkflow.java` with the following content:
   ```java
   package com.lucasbdourado.autotimemarking.modules.scheduler.domain;

   public interface MarkingWorkflow {
       /**
        * Executes one check and marking cycle on BMAquiosque.
        * Throws Exception on failure to allow scheduler to track errors.
        */
       void executeMarkingCycle() throws Exception;
   }
   ```
3. Run `mvn clean compile` in the workspace root to verify that the file compiles successfully.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| The interface `com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow` is created. | Full | The file `MarkingWorkflow.java` exists at the correct path. |
| The method `executeMarkingCycle()` is declared and throws `Exception`. | Full | Verification of the code structure in `MarkingWorkflow.java`. |
| The codebase compiles successfully. | Full | Successful compilation of the project via `mvn clean compile`. |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Compilation check | Manual / Build | Verify that the Java compiler builds the interface without errors. | Run `mvn clean compile`. |

## Dependencies

- None.

## Risks and Edge Cases

- None. This is a simple Java interface declaration.

## Rollback or Recovery Notes

- Delete the created `MarkingWorkflow.java` file and clean the empty packages if required.

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
- [x] Acceptance criteria mapping defined.
- [x] Tests and validation strategy defined.
- [x] Risks and rollback notes documented.

## Notes for Execute Task

- Do not add `@Component` or Spring DI annotations to `MarkingWorkflow`. It is a pure domain interface.
- Ensure package name is exactly `com.lucasbdourado.autotimemarking.modules.scheduler.domain`.
