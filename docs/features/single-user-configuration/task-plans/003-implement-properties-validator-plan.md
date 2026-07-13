# Task Implementation Plan: Implement Properties Validator

## Status

Status: Ready for Implementation

Last updated: 2026-07-13

Plan file: `docs/features/single-user-configuration/task-plans/003-implement-properties-validator-plan.md`

## Task Reference

Task ID: `TSK-SUC-003`

Task file: `docs/features/single-user-configuration/tasks/003-implement-properties-validator.md`

Task status: `Depends on Previous Task` (Prerequisite is complete in codebase)

## Feature Reference

Feature name: `single-user-configuration`

Feature file: `docs/features/single-user-configuration/feature.md`

Feature Tech Spec: `docs/features/single-user-configuration/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/single-user-configuration/tasks/003-implement-properties-validator.md` | Goal, Scope, Acceptance Criteria | Confirmed | Primary source for task bounds |
| Feature file | `docs/features/single-user-configuration/feature.md` | Feature Goal, Scope | Confirmed | Functional context |
| Feature Tech Spec | `docs/features/single-user-configuration/tech-spec.md` | Proposed Technical Approach, State/Error Handling, Validation Rules | Confirmed | Primary technical design source |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions, Guidelines | Confirmed | Stack constraints (Java 21, Spring Boot) |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Configuration, Infrastructure Layer | Confirmed | Modular package structure conventions |

## Planning Scope

This planning session is scoped only to the creation of the `BmaquiosquePropertiesValidator` class. It does not cover the execution hook integration (Task 004), log message masking (Task 005), or unit/integration testing implementation (Task 006).

## Task Summary

Implement the class `BmaquiosquePropertiesValidator` as a Spring-managed `@Component` under the infrastructure package to validate `BmaquiosqueProperties` instances. It delegates basic validation to the Jakarta Bean Validation API and implements logical validation for timezone and max entry time (format and boundaries).

## Execution Eligibility

Status: Eligible

Reason: The prerequisite task `002-implement-bmaquiosque-properties.md` has been successfully implemented in the codebase (the properties class `BmaquiosqueProperties` is created and annotated). This task is ready to be executed once this plan is approved.

## Feature Context

The backend service requires strict validation of configuration settings upon boot to ensure it does not run scheduling loops or automation steps with invalid parameters. This class encapsulates all technical validation rules before they are bound into the startup lifecycle.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach | Full | Yes | Defines two-tiered validation (declarative + logical). |
| Data Contracts | Full | Yes | Defines property fields and custom boundaries. |
| State and Error Handling | Partial | Yes | Defines error message mappings for each rule. Actual context termination is handled in Task 004. |
| Validation Rules | Full | Yes | Implements logical rules (MVP-VR-001, MVP-VR-002, MVP-VR-003, and timezone validation). |

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 (LTS) | `technology-definition.md` | Target runtime compile target. |
| Spring Boot 3.4.x | `technology-definition.md` | Enables component scanning and dependency injection. |
| Spring Boot Starter Validation | `pom.xml` / `technology-definition.md` | Provides the JSR-380 `jakarta.validation.Validator` instance. |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Infrastructure Layer Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Class packaging and framework dependency | Place validator under infrastructure configuration package. |

## Existing Decisions Reviewed

No existing feature, ADR, or architecture decision was relevant to this task besides the confirmed technology definition.

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` | Properties fields and annotations | Target of validation | Validator will evaluate these fields: `username`, `password`, `jitterMinutes`, `maxEntryTime`, `timezone`. |
| `pom.xml` | Dependencies | Validation libraries | Confirmed presence of `spring-boot-starter-validation` dependency. |

## Confirmed Scope

- Create `BmaquiosquePropertiesValidator` in package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Annotate with Spring `@Component`.
- Inject standard `jakarta.validation.Validator`.
- Map JSR-380 violations to the specific error messages:
  - Blank `username` or `password` -> `"credentials cannot be blank."`
  - Negative/null `jitterMinutes` -> `"jitter-minutes must be a non-negative integer."`
- Implement timezone validation using `ZoneId.of(...)` (catching `DateTimeException` / `ZoneRulesException`). On error, add `"timezone is invalid."`.
- Implement max entry time format validation using `LocalTime.parse(..., DateTimeFormatter.ofPattern("HH:mm"))` (catching `DateTimeParseException`). On error, add `"max-entry-time must be in HH:mm format."`.
- Implement max entry time boundary validation: verify parsed time is between `05:00` and `22:00` inclusive. On error, add `"max-entry-time must be between 05:00 and 22:00."`.
- Expose method `public List<String> validate(BmaquiosqueProperties properties)` returning all accumulated errors.

## Out of Scope

- Integrating validation into the Spring startup lifecycle (Task 004).
- Formatting success logs or masking passwords (Task 005).
- Implementing tests (Task 006).

## Proposed Implementation Approach

1. Create `BmaquiosquePropertiesValidator` and annotate with `@Component`.
2. Construct the class to accept standard `jakarta.validation.Validator` via constructor injection.
3. In `validate(BmaquiosqueProperties properties)`, initialize a `List<String> errors = new ArrayList<>()`.
4. Validate properties using `validator.validate(properties)` and iterate over violations. Check the property path:
   - If property path is `username` or `password`, add `"credentials cannot be blank."`
   - If property path is `jitterMinutes`, add `"jitter-minutes must be a non-negative integer."`
5. Check if timezone string is null or blank. If so, add `"timezone is invalid."`. Otherwise, try to parse it with `ZoneId.of(timezone)`. Catch any `DateTimeException` or `ZoneRulesException` and add `"timezone is invalid."`.
6. Check if max entry time string is null or blank. If so, add `"max-entry-time must be in HH:mm format."`. Otherwise, try to parse it with `LocalTime.parse(maxEntryTime, DateTimeFormatter.ofPattern("HH:mm"))`.
   - On `DateTimeParseException`, add `"max-entry-time must be in HH:mm format."`.
   - If parsing succeeds, check if parsed time is before `05:00` or after `22:00`. If so, add `"max-entry-time must be between 05:00 and 22:00."`.
7. Return the `errors` list.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidator.java` | Create | Confirmed | Task Scope | New validator class |

## Implementation Steps

1. Create the class file `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidator.java`.
2. Add imports for standard validation and Java time classes.
3. Implement `BmaquiosquePropertiesValidator` with `@Component` and constructor-injected `jakarta.validation.Validator`.
4. Implement `public List<String> validate(BmaquiosqueProperties properties)`.
5. Map constraints and parse timezone/time values as planned.
6. Verify compilation by running `mvn clean compile`.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Class `BmaquiosquePropertiesValidator` compiles and correctly evaluates both standard and custom constraints | Implement JSR-380 validation delegation and custom timezone/time validations in class | `mvn clean compile` succeeds |
| Valid configurations pass without error | A valid configuration returns an empty error list | Handled in validator logic (will be tested in Task 006) |
| Invalid configurations result in detailed validation errors indicating the specific constraint violation | Invalid fields result in correct error strings added to returned list | Handled in validator logic (will be tested in Task 006) |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `mvn clean compile` | Build validation | Ensure the class compiles cleanly within the project structure | Execute command during task verification |

## Dependencies

- **Prerequisite**: Task 002 `002-implement-bmaquiosque-properties.md` (implemented).
- **Successor**: Task 004 `004-implement-verification-hook.md` (depends on this validator class to run checks during application context bootstrap).

## Risks and Edge Cases

- **Null Properties Input**: If the properties bean itself is null, the validator should throw an `IllegalArgumentException`. We will add a null-check at the beginning of the `validate` method.
- **Multiple Time Exceptions**: `ZoneId.of` throws `DateTimeException` which covers `ZoneRulesException`. Catching `DateTimeException` is sufficient and safe.

## Rollback or Recovery Notes

- Delete the file `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidator.java`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Decisions Created During Planning

No local feature/task decisions were created during this planning session.

## Task Planning Readiness Checklist

- [x] Task file reviewed.
- [x] Feature context reviewed.
- [x] Feature Tech Spec coverage verified.
- [x] Technology decisions reviewed.
- [x] Applicable guidelines reviewed.
- [x] Existing decisions reviewed.
- [x] Local codebase references checked when applicable.
- [x] Task dependencies checked.
- [x] Execution eligibility documented.
- [x] Blocking decisions resolved.
- [x] Local feature/task decisions documented when needed.
- [x] Architecture/global decisions routed to ADR or `resolve-architecture-blocker` when needed.
- [x] Implementation approach defined.
- [x] Acceptance criteria mapped.
- [x] Tests and validation strategy defined.
- [x] Risks and rollback notes documented.

## Notes for Execute Task

- Ensure the package declaration is exactly `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Do not add any startup hook or log message print statements in this validator class.
- Return the error messages precisely as specified in the state and error handling section of the Tech Spec (without the `BMAquiosque configuration error: ` prefix, as the hook will prepend it when logging/printing).
