# Task: Implement Marking Workflow Orchestrator

## Status

Pending

## Task ID

TSK-MC-004

## Feature

`docs/features/marking-calculation/feature.md`

## Source Documents

- `docs/features/marking-calculation/feature.md`
- `docs/features/marking-calculation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `MarkingWorkflowOrchestrator` class that implements `MarkingWorkflow` (from `activity-scheduler` domain) and coordinates status fetching, calculation evaluation, and punch registration.

## Context

The `ActivityScheduler` invokes `MarkingWorkflow.executeMarkingCycle()` every 30 minutes. This task implements the bridge connecting `TimeClockClient` (browser automation) and `MarkingCalculatorService` (business logic).

## Scope

- Create class `MarkingWorkflowOrchestrator` under package `com.lucasbdourado.autotimemarking.modules.workflow.service`.
- Annotate with `@Service` and `@Primary` implementing `MarkingWorkflow`.
- Autowire `TimeClockClient`, `MarkingCalculatorService`, `TimeJitterService`, and `BmaquiosqueProperties`.
- Implementation logic for `executeMarkingCycle()`:
  1. Retrieve `WorkdayState` from `timeClockClient.getTodayMarkings()`.
  2. Compute random jitter using `timeJitterService`.
  3. Evaluate decision via `markingCalculatorService.evaluate(...)`.
  4. Log decision summary (type, target time, jitter, decision reason).
  5. If `decision.shouldPunch()` is true, trigger `timeClockClient.registerMarking(decision.nextType())` and log completion.
  6. If false, log skip status.

## Out of Scope

- Direct Playwright browser logic (handled by `TimeClockClient` implementation).

## Depends On

- `TSK-MC-001 - Define Marking Domain Models`
- `TSK-MC-002 - Implement Workday Calculation Engine`
- `TSK-MC-003 - Implement Time Jitter Service`
- `TSK-BMA-004 - Implement TimeClockClient Interface` (from `bmaquiosque-automation`)

## Acceptance Criteria

- [ ] `MarkingWorkflowOrchestrator` implements `MarkingWorkflow` and orchestrates check, evaluation, and punch execution.
- [ ] Code compiles cleanly with `mvn clean compile`.
