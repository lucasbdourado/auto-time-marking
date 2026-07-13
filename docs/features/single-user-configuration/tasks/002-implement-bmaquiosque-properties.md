# Task: Implement Bmaquiosque Properties

## Status

Depends on Previous Task

## Task ID

TSK-SUC-002

## Feature

`docs/features/single-user-configuration/feature.md`

## Source Documents

- `docs/features/single-user-configuration/feature.md`
- `docs/features/single-user-configuration/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create the `@ConfigurationProperties` binding bean `BmaquiosqueProperties` and configure standard JSR-380 validation annotations on its fields.

## Context

The application needs to map configuration values (username, password, jitter, max entry time, timezone) from `application.properties` or environment variables into a strongly-typed Spring bean.

## Scope

- Create the `BmaquiosqueProperties` Java class under package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Use `@ConfigurationProperties(prefix = "bmaquiosque")` to bind properties.
- Define fields and add JSR-380 validation annotations:
  - `username` (String) -> `@NotBlank`
  - `password` (String) -> `@NotBlank`
  - `maxEntryTime` (String) -> bound as String (logical validation to be implemented in Task 003)
  - `jitterMinutes` (Integer) -> `@NotNull`, `@Min(0)`
  - `timezone` (String) -> default to `America/Sao_Paulo` if not explicitly specified
- Configure `src/main/resources/application.properties` to map properties to system environment variables with empty fallback (e.g., `bmaquiosque.username=${BMAQUIOSQUE_USERNAME:}`).

## Out of Scope

- Implementing the custom logical validator (`BmaquiosquePropertiesValidator`).
- Setting up the startup verification hook.

## Depends On

001-setup-project-chassis.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- Class `BmaquiosqueProperties` exists under the correct package and contains the specified fields and JSR-380 validation annotations.
- `application.properties` specifies the keys and maps them to environment variables.
- The project compiles successfully.

## Implementation Notes

- Use standard Spring Boot configuration binding annotations (`@ConfigurationProperties`, `@Configuration`).
- Target properties prefix is `bmaquiosque`.

## Validation Notes

- Run `mvn clean compile` to check that the newly added configuration class compiles correctly.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
