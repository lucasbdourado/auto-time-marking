# Task: Implement Configuration Tests

## Status

Depends on Previous Task

## Task ID

TSK-SUC-006

## Feature

`docs/features/single-user-configuration/feature.md`

## Source Documents

- `docs/features/single-user-configuration/feature.md`
- `docs/features/single-user-configuration/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create unit and integration tests to verify property validations, custom boundary rules, logging formats, and startup exit behaviors.

## Context

Tests guarantee that validation constraints are maintained and prevent regression during future updates or dependency upgrades.

## Scope

- Create JUnit 5 unit tests for `BmaquiosquePropertiesValidator` under `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/`.
- Cover the following scenarios in unit tests:
  - Blank/null/empty `username` or `password`.
  - Negative values for `jitterMinutes`.
  - Invalid formats for `maxEntryTime` (e.g., `9:00`, `25:00`, `abc`).
  - Out-of-bounds `maxEntryTime` (e.g., `04:59` and `22:01`, boundaries are `05:00` and `22:00` inclusive).
  - Invalid `timezone` IDs (e.g., `GMT+25`, `Invalid/Timezone`).
  - Completely valid properties configurations.
- Create integration tests using `@SpringBootTest` to verify that Spring Boot fails to start (e.g. context fails to load) when booted with invalid configurations.

## Out of Scope

- Testing database connections, automation, or scheduling behaviors (these belong to other feature test suites).

## Depends On

005-implement-logging-and-masking.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- All test cases are implemented using JUnit 5 and Spring Boot Test starters.
- Test suites run and pass cleanly via `mvn test`.
- All properties validation constraints and timezone checks have test coverage.

## Implementation Notes

- Use `@SpringBootTest` with `@TestPropertySource` or `@SpringBootTest(properties = {...})` to inject test-specific configurations during integration test context boot.

## Validation Notes

- Run `mvn test` to verify all test suites are executed and pass.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
