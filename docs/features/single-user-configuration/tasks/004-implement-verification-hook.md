# Task: Implement Verification Hook

## Status

Implemented

## Task ID

TSK-SUC-004

## Feature

`docs/features/single-user-configuration/feature.md`

## Source Documents

- `docs/features/single-user-configuration/feature.md`
- `docs/features/single-user-configuration/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create the `ConfigurationVerificationHook` startup lifecycle bean to trigger property validation on boot and exit the JVM on failure.

## Context

We need to guarantee a fail-fast startup behavior. If any configuration parameter is invalid or missing, the application must print the errors and terminate immediately.

## Scope

- Create the `ConfigurationVerificationHook` class under package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Implement a Spring lifecycle callback (e.g., implementing `CommandLineRunner`, `InitializingBean`, or using a `@PostConstruct` block).
- Inject `BmaquiosqueProperties` and `BmaquiosquePropertiesValidator`.
- Execute validation checks during the bootstrap sequence.
- On validation failure, log all errors to the console/stderr and terminate the application context with a non-zero exit code (e.g., by throwing `IllegalStateException` or calling `System.exit(1)`).

## Out of Scope

- Log masking for passwords (this is handled in Task 005).
- Writing configuration tests.

## Depends On

003-implement-properties-validator.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `ConfigurationVerificationHook` executes automatically during Spring Boot application startup.
- If properties are invalid, the bootstrap sequence is interrupted and terminates with a non-zero exit code.
- If properties are valid, the application starts normally.

## Implementation Notes

- Intercepting validation errors at startup is critical to prevent other services (like Playwright browser automation or the scheduled marking checks) from starting with invalid credentials or configurations.

## Validation Notes

- Run `mvn clean compile` to ensure the hook class compiles and integrates with the properties package.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
