# Task: Write Calculation Unit Tests

## Status

Pending

## Task ID

TSK-MC-005

## Feature

`docs/features/marking-calculation/feature.md`

## Source Documents

- `docs/features/marking-calculation/feature.md`
- `docs/features/marking-calculation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create comprehensive unit test suites for `MarkingCalculatorService`, `TimeJitterService`, and `MarkingWorkflowOrchestrator`.

## Context

Calculation logic must be thoroughly validated across all business rule permutations (entry bounds, 6h lunch limit, variable 1h-2h lunch duration, exit calculation, jitter constraints) using isolated unit tests.

## Scope

- Create `MarkingCalculatorTest` under package `com.lucasbdourado.autotimemarking.modules.calculation.service`:
  - Test 0 markings -> ENTRY decision.
  - Test 1 marking -> LUNCH_OUT decision.
  - Test 2 markings -> LUNCH_RETURN decision.
  - Test 3 markings -> EXIT decision with exact 8h45 work calculation.
  - Test 3 markings with 1h30 lunch -> exit recalculated accordingly.
  - Test 4 markings -> noPunch decision.
- Create `TimeJitterServiceTest` under package `com.lucasbdourado.autotimemarking.modules.calculation.service`:
  - Test jitter range limits.
- Create `MarkingWorkflowOrchestratorTest` under package `com.lucasbdourado.autotimemarking.modules.workflow.service`:
  - Test orchestrator workflow with Mockito mocks for `TimeClockClient` and `MarkingCalculatorService`.

## Out of Scope

- End-to-end browser automation tests.

## Depends On

- `TSK-MC-002 - Implement Workday Calculation Engine`
- `TSK-MC-003 - Implement Time Jitter Service`
- `TSK-MC-004 - Implement Marking Workflow Orchestrator`

## Acceptance Criteria

- [ ] Unit test classes written and green.
- [ ] `mvn clean test` runs all tests with 100% pass rate.
