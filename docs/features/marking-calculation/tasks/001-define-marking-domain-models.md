# Task: Define Marking Domain Models

## Status

Pending

## Task ID

TSK-MC-001

## Feature

`docs/features/marking-calculation/feature.md`

## Source Documents

- `docs/features/marking-calculation/feature.md`
- `docs/features/marking-calculation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Define domain enums and immutable Java 21 `record` types (`MarkingType`, `MarkingRecord`, `WorkdayState`, `PunchDecision`) required for the marking calculation engine.

## Context

The calculation engine requires clean, immutable value objects to represent punches registered on BMAquiosque, the current workday state, and evaluation results.

## Scope

- Create package `com.lucasbdourado.autotimemarking.modules.calculation.domain`.
- Define `MarkingType` enum with sequence numbers and labels for `ENTRY`, `LUNCH_OUT`, `LUNCH_RETURN`, `EXIT`.
- Define `MarkingRecord` record containing `MarkingType type` and `LocalTime time`.
- Define `WorkdayState` record containing `List<MarkingRecord> existingMarkings` with helper methods (`hasMarking`, `getMarking`).
- Define `PunchDecision` record containing `boolean shouldPunch`, `MarkingType nextType`, `LocalTime calculatedTargetTime`, `LocalTime jitteredTargetTime`, and `String reason`, along with static factory methods (`noPunch`, `execute`).

## Out of Scope

- Calculation logic algorithm (handled in TSK-MC-002).

## Depends On

- None.

## Acceptance Criteria

- [ ] `MarkingType`, `MarkingRecord`, `WorkdayState`, and `PunchDecision` are defined under package `com.lucasbdourado.autotimemarking.modules.calculation.domain`.
- [ ] Code compiles cleanly with `mvn clean compile`.
