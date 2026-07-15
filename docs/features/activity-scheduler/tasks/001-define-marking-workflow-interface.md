# Task: Define MarkingWorkflow Interface

## Status

Implemented

## Task ID

TSK-AS-001

## Feature

`docs/features/activity-scheduler/feature.md`

## Source Documents

- `docs/features/activity-scheduler/feature.md`
- `docs/features/activity-scheduler/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create the `MarkingWorkflow` interface in the domain package of the scheduler module.

## Context

The scheduler triggers a workday status check and marking execution cycle every 30 minutes, but it should not know the implementation details of browser automation or work time logic. Decoupling this is done by defining the `MarkingWorkflow` interface.

## Scope

- Create package `com.lucasbdourado.autotimemarking.modules.scheduler.domain`.
- Create interface `MarkingWorkflow` containing the signature:
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

## Out of Scope

- Implementing the actual browser automation workflow or business logic calculations.

## Depends On

None

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- The interface `com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow` is created.
- The method `executeMarkingCycle()` is declared and throws `Exception`.
- The codebase compiles successfully.

## Implementation Notes

- Follow the confirmed stack and constraints from `docs/architecture/auto-time-marking/technology-definition.md`.
- Keep the work limited to this feature and task scope.
- Do not introduce new architecture, libraries, persistence, API contracts, or product behavior unless already defined in the source documents.
- If implementation requires an undocumented decision, keep the task blocked or defer the decision to `plan-task`.

## Validation Notes

- Run `mvn clean compile` to ensure compilation.

## Risks

- None.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
