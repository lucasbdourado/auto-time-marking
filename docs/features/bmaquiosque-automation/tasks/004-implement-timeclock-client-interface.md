# Task: Implement TimeClockClient Interface

## Status

Ready

## Task ID

TSK-BMA-004

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create the domain port interface `TimeClockClient` to decouple the automation mechanics from calculation and scheduling modules.

## Context

Following Clean Architecture conventions, we must isolate external technology implementations (like Playwright browser automation) behind a clean domain interface interface.

## Scope

- Create a new Java interface [TimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/domain/TimeClockClient.java) under the package `com.lucasbdourado.autotimemarking.modules.automation.domain`.
- Declare the following methods with Javadoc documentation:
  - `List<LocalTime> retrieveDailyMarkings(String username, String password) throws Exception;`
  - `void registerMarking(String username, String password) throws Exception;`

## Out of Scope

- Implementing the Playwright-based client class or the retry decorator.

## Depends On

None

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- Interface `TimeClockClient` exists with the exact signatures defined in the technical specification.
- The interface compiles successfully.

## Implementation Notes

- Avoid importing any Playwright or third-party web driver dependencies in this package, as it belongs to the domain layer.
- Keep `LocalTime` and `List` imports standard (`java.time.LocalTime` and `java.util.List`).

## Validation Notes

- Run `mvn clean compile` to check that the newly created interface compiles correctly.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
