# Task: Implement Time Jitter Service

## Status

Pending

## Task ID

TSK-MC-003

## Feature

`docs/features/marking-calculation/feature.md`

## Source Documents

- `docs/features/marking-calculation/feature.md`
- `docs/features/marking-calculation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `TimeJitterService` component to generate pseudo-random time variations (jitter) within configured bounds.

## Context

To avoid bot-detection patterns, punch registrations must inject a small random time offset (e.g. ±5 minutes) relative to computed target times.

## Scope

- Create interface `TimeJitterService` and implementation `RandomTimeJitterService` under `com.lucasbdourado.autotimemarking.modules.calculation.service`.
- Implement `generateJitterMinutes(int maxJitterMinutes)` returning an integer between `-maxJitterMinutes` and `+maxJitterMinutes`.
- Provide overload allowing custom `Random` instance injection to support deterministic unit testing.

## Out of Scope

- Core workday punch decision tree (handled in TSK-MC-002).

## Depends On

- `TSK-MC-001 - Define Marking Domain Models`

## Acceptance Criteria

- [ ] `TimeJitterService` returns integer values within `[-maxJitterMinutes, +maxJitterMinutes]`.
- [ ] Code compiles cleanly with `mvn clean compile`.
