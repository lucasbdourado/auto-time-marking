# Task Implementation Plan: Implement Verification Hook

## Status

Status: Ready for Implementation

Last updated: 2026-07-13

Plan file: `docs/features/single-user-configuration/task-plans/004-implement-verification-hook-plan.md`

## Task Reference

Task ID: `TSK-SUC-004`

Task file: `docs/features/single-user-configuration/tasks/004-implement-verification-hook.md`

Task status: `Depends on Previous Task` (Prerequisite is complete in codebase)

## Feature Reference

Feature name: `single-user-configuration`

Feature file: `docs/features/single-user-configuration/feature.md`

Feature Tech Spec: `docs/features/single-user-configuration/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/single-user-configuration/tasks/004-implement-verification-hook.md` | Goal, Scope, Acceptance Criteria | Confirmed | Primary source for task bounds |
| Feature file | `docs/features/single-user-configuration/feature.md` | Feature Goal, Scope | Confirmed | Functional context |
| Feature Tech Spec | `docs/features/single-user-configuration/tech-spec.md` | Proposed Technical Approach, State/Error Handling, Validation Rules | Confirmed | Primary technical design source |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions, Guidelines | Confirmed | Stack constraints (Java 21, Spring Boot) |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Configuration, Infrastructure Layer | Confirmed | Modular package structure conventions |

## Planning Scope

This planning session is scoped only to the creation of the `ConfigurationVerificationHook` class and its integration into the Spring boot lifecycle for fail-fast configuration checks. It does not cover success logging and password masking (Task 005) or writing tests (Task 006).

## Task Summary

Implement the class `ConfigurationVerificationHook` under the configuration infrastructure package as an `InitializingBean`. It executes the `BmaquiosquePropertiesValidator` validation rules on application startup and halts the context by throwing an `IllegalStateException` containing validation errors.

## Execution Eligibility

Status: Eligible

Reason: The prerequisite task `003-implement-properties-validator.md` has been successfully implemented in the codebase (the validator class `BmaquiosquePropertiesValidator` is created and scanable). This task is ready to be executed once this plan is approved.

## Feature Context

The application needs to fail-fast if started with invalid configurations (credentials, jitter, max entry time, or timezone) to prevent active schedulers or automated browser sessions from executing with invalid parameters.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach | Full | Yes | Implements Spring lifecycle callback to trigger validation and throw custom exception to terminate context. |
| State and Error Handling | Full | Yes | Iterates validator errors, logs with prefix `BMAquiosque configuration error: `, and aborts context boot. |
| Validation Rules | Full | Yes | Enforces JSR-380 and logical validations via the validator class at startup. |

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 (LTS) | `technology-definition.md` | Target runtime compile target. |
| Spring Boot 3.4.x | `technology-definition.md` | Component scanning, DI, and lifecycle interface (`InitializingBean`). |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Infrastructure Layer Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Class packaging and framework dependency | Place hook class under infrastructure configuration package. |

## Existing Decisions Reviewed

| Decision | Path | Relevance |
| --- | --- | --- |
| Hook Mechanism & Exception Choice | Current session user confirmation (1) | Confirmed using `InitializingBean` lifecycle interface and throwing `IllegalStateException` on error to fail-fast cleanly and preserve testability. |

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidator.java` | Class definition and method `validate` | Component to trigger | Inject and execute `validate(BmaquiosqueProperties)` |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` | Properties fields | Target to validate | Injected into the hook class |

## Confirmed Scope

- Create `ConfigurationVerificationHook` in package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Annotate with Spring `@Component`.
- Implement `org.springframework.beans.factory.InitializingBean` interface.
- Inject `BmaquiosqueProperties` and `BmaquiosquePropertiesValidator` via constructor injection.
- Implement `afterPropertiesSet()` to:
  - Call `validator.validate(properties)`.
  - If there are errors, log each validation error using SLF4J logger at ERROR level prefixed with `"BMAquiosque configuration error: "`.
  - Throw `IllegalStateException("BMAquiosque configuration validation failed")` to halt startup.

## Out of Scope

- Log masking for passwords (this is handled in Task 005).
- Success logging when properties are valid (this is handled in Task 005).
- Writing configuration tests (this is handled in Task 006).

## Proposed Implementation Approach

1. Create class `ConfigurationVerificationHook` implementing `InitializingBean`.
2. Add constructor-injection for both `BmaquiosqueProperties` and `BmaquiosquePropertiesValidator`.
3. In `afterPropertiesSet()`, execute `validator.validate(properties)`.
4. If errors are returned:
   - For each error, log at error level using SLF4J: `logger.error("BMAquiosque configuration error: {}", error)`.
   - Throw `new IllegalStateException("BMAquiosque configuration validation failed")`.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationVerificationHook.java` | Create | Confirmed | Task Scope | New lifecycle bean class |

## Implementation Steps

1. Create class `ConfigurationVerificationHook` under package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
2. Implement `InitializingBean` and inject properties and validator.
3. Call validator and process error logging/throwing exception in `afterPropertiesSet()`.
4. Compile the project with `mvn clean compile` to ensure there are no compilation errors.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| `ConfigurationVerificationHook` executes automatically during Spring Boot application startup | Bean implements `InitializingBean` and is annotated with `@Component` | Validated by context load (compilation & test boot) |
| If properties are invalid, the bootstrap sequence is interrupted and terminates with a non-zero exit code | `afterPropertiesSet()` throws `IllegalStateException` on validation error | Throwing this exception fails Spring boot startup with code > 0 |
| If properties are valid, the application starts normally | Hook proceeds silently without throwing exceptions | Application context starts successfully |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `mvn clean compile` | Build validation | Ensure the class compiles cleanly and package/imports are correct | Execute command during task verification |

## Dependencies

- **Prerequisite**: Task 003 `003-implement-properties-validator.md` (implemented).
- **Successor**: Task 005 `005-implement-logging-and-masking.md` (will implement success logging and password masking on top of this hook).

## Risks and Edge Cases

- **Circular Dependency**: If `BmaquiosquePropertiesValidator` or `BmaquiosqueProperties` depend on this hook, it could fail context creation. They do not depend on it (the hook is a terminal leaf of the configuration loading module), so this is safe.

## Rollback or Recovery Notes

- Delete the file `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationVerificationHook.java`.

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
- Do not implement any logging for successful config validation in this task, as it is scoped strictly to Task 005.
