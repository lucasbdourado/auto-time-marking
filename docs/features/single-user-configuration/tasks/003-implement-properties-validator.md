# Task: Implement Properties Validator

## Status

Depends on Previous Task

## Task ID

TSK-SUC-003

## Feature

`docs/features/single-user-configuration/feature.md`

## Source Documents

- `docs/features/single-user-configuration/feature.md`
- `docs/features/single-user-configuration/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create the custom validator `BmaquiosquePropertiesValidator` to handle logical validations of time configuration values (max-entry-time formats, timezone).

## Context

Standard JSR-380 annotations cannot validate custom time ranges, format parsing, or verify timezone validity. A custom validator class is needed to ensure all inputs are logically correct.

## Scope

- Create the `BmaquiosquePropertiesValidator` class under package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Integrate JSR-380 validator (e.g., Spring's `Validator` or standard Jakarta `Validator`) to check basic annotations.
- Implement timezone validation: verify `timezone` is a valid `java.time.ZoneId` using `ZoneId.of(timezone)` and catching `ZoneRulesException` / `DateTimeException`.
- Implement time format validation: verify `maxEntryTime` is in `HH:mm` format (using `LocalTime.parse` with `DateTimeFormatter.ofPattern("HH:mm")` and catching `DateTimeParseException`).
- Implement time boundary validation: verify parsed `maxEntryTime` is between `05:00` and `22:00` (inclusive).
- Return or throw a structured list of validation errors when validation fails.

## Out of Scope

- Integrating validation into the Spring startup lifecycle (this is handled in Task 004).
- Formatting success logs or masking passwords.

## Depends On

002-implement-bmaquiosque-properties.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- Class `BmaquiosquePropertiesValidator` compiles and correctly evaluates both standard and custom constraints.
- Valid configurations pass without error.
- Invalid configurations result in detailed validation errors indicating the specific constraint violation.

## Implementation Notes

- Max entry time boundary rule: `[05:00, 22:00]` inclusive.
- Timezone must be a valid ID recognized by `java.time.ZoneId`.

## Validation Notes

- Run `mvn clean compile` to check that the newly added validator class compiles successfully.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
