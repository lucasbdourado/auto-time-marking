# Task: Implement Workday Calculation Engine

## Status

Pending

## Task ID

TSK-MC-002

## Feature

`docs/features/marking-calculation/feature.md`

## Source Documents

- `docs/features/marking-calculation/feature.md`
- `docs/features/marking-calculation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `MarkingCalculatorService` component that evaluates the current `WorkdayState` and computes punch decisions.

## Context

The core business logic of Auto Time Marking determines which punch is needed next, calculates target times based on 8h45 work limits, max entry time, 6h lunch limit, and variable lunch duration (1h to 2h), and decides whether a punch should trigger.

## Scope

- Create interface `MarkingCalculatorService` and implementation `DefaultMarkingCalculatorService` under `com.lucasbdourado.autotimemarking.modules.calculation.service`.
- Implement decision rules:
  1. No markings exist -> `ENTRY` target is `maxEntryTime` (from config).
  2. 1 marking (`ENTRY`) exists -> `LUNCH_OUT` target is `entryTime + 6 hours`.
  3. 2 markings (`ENTRY`, `LUNCH_OUT`) exist -> `LUNCH_RETURN` target is `lunchOutTime + 1 hour`.
  4. 3 markings (`ENTRY`, `LUNCH_OUT`, `LUNCH_RETURN`) exist -> `EXIT` target is `entryTime + actualLunchDuration + 525 minutes` (8h45 effective work).
  5. 4 markings exist -> `noPunch`.
- Apply jitter offset to calculate `jitteredTargetTime` and evaluate `shouldPunch = currentTime >= jitteredTargetTime`.

## Out of Scope

- Random jitter generation algorithm (handled in TSK-MC-003).
- Playwright browser interactions (handled in `bmaquiosque-automation`).

## Depends On

- `TSK-MC-001 - Define Marking Domain Models`

## Acceptance Criteria

- [ ] `MarkingCalculatorService` evaluates all 4 punch stages accurately according to business rules.
- [ ] Exit calculation dynamically recalculates target time when actual lunch duration exceeds 1 hour.
- [ ] Code compiles cleanly with `mvn clean compile`.
