# Task: Add Playwright Dependency

## Status

Implemented

## Task ID

TSK-BMA-001

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Add Playwright Java dependency to the project's Maven configuration (`pom.xml`).

## Context

Playwright for Java is the selected browser automation framework. To implement the time clock client, the library must be added to the project's `pom.xml`.

## Scope

- Open the project [pom.xml](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/pom.xml).
- Add the following dependency inside the `<dependencies>` block:
  ```xml
  <dependency>
      <groupId>com.microsoft.playwright</groupId>
      <artifactId>playwright</artifactId>
      <version>1.49.0</version>
  </dependency>
  ```

## Out of Scope

- Implementing the automation client or selectors.
- Setting up the scheduling or task execution.

## Depends On

None

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- Playwright dependency is successfully defined in `pom.xml`.
- Running `mvn clean compile` succeeds without dependency download errors.

## Implementation Notes

- Use the official coordinates `com.microsoft.playwright:playwright` with version `1.49.0` as detailed in the technology reference.

## Validation Notes

- Run `mvn clean compile` to check that the dependencies resolve and compile correctly.

## Risks

- Slow initial build or network timeout downloading browser binaries during compilation (Playwright downloads binaries on first launch/setup, but the Maven artifact itself must be resolved).

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
