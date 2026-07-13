# Task Implementation Plan: Implement Configuration Tests

## Status

Status: Ready for Implementation

Last updated: 2026-07-13

Plan file: `docs/features/single-user-configuration/task-plans/006-implement-configuration-tests-plan.md`

## Task Reference

Task ID: `TSK-SUC-006`

Task file: `docs/features/single-user-configuration/tasks/006-implement-configuration-tests.md`

Task status: `Depends on Previous Task` (Prerequisite is complete in codebase)

## Feature Reference

Feature name: `single-user-configuration`

Feature file: `docs/features/single-user-configuration/feature.md`

Feature Tech Spec: `docs/features/single-user-configuration/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/single-user-configuration/tasks/006-implement-configuration-tests.md` | Goal, Scope, Acceptance Criteria | Confirmed | Primary source for task bounds |
| Feature file | `docs/features/single-user-configuration/feature.md` | Feature Completion Criteria | Confirmed | Functional context |
| Feature Tech Spec | `docs/features/single-user-configuration/tech-spec.md` | Testing Strategy, Proposed Technical Approach | Confirmed | Primary technical design source |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions (Testing) | Confirmed | Stack constraints (JUnit 5, Spring Boot Test) |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Package structure conventions | Confirmed | Modular package structure |

## Planning Scope

This planning session is scoped only to implementing JUnit 5 unit tests for `BmaquiosquePropertiesValidator` and integration tests using `@SpringBootTest` to verify application startup exit behaviors when configurations are invalid. It does not cover testing database connections, scheduling execution logic, or automation scheduling behaviors (deferred to other feature test suites).

## Task Summary

Create a comprehensive test suite with JUnit 5 unit tests covering validator constraints (null/blank username/password, negative jitter, invalid format and out-of-bounds max entry time, invalid timezone) and `@SpringBootTest` integration tests verifying application startup failure with invalid configurations.

## Execution Eligibility

Status: Eligible

Reason: The prerequisite task `005-implement-logging-and-masking.md` has been successfully implemented in the codebase.

## Feature Context

To ensure the application behaves predictably and fails fast on invalid startup parameters, we must have a complete set of unit and integration tests. This prevents configuration-related regressions during downstream developments or dependency upgrades.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Testing Strategy | Full | Yes | Defines JUnit 5 unit tests for validator and `@SpringBootTest` integration tests for startup failure. |
| Validation Rules | Full | Yes | Ensures all VR rules (MVP-VR-001, MVP-VR-002, MVP-VR-003, Timezone check) have dedicated test cases. |

Coverage assessment:
- Justifying Tech Spec section: `Testing Strategy`
- Tech Spec sections implemented by this task: `Testing Strategy` and `Validation Rules`
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 (LTS) | `technology-definition.md` | Target runtime / compiler target for test code. |
| Maven | `technology-definition.md` | Runs tests via `mvn test`. |
| Spring Boot Test Starter (JUnit 5 + Spring Boot Test) | `technology-definition.md` | Core test libraries and annotations. |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Infrastructure Layer Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Package structure conventions | Test classes must live under the corresponding test package: `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config` in `src/test/java`. |

## Existing Decisions Reviewed

No existing feature, ADR, or architecture decision was relevant to this task besides the confirmed technology definition.

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidator.java` | Class validation methods | Unit test target | Read validation logic to cover all branches. |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationVerificationHook.java` | Context startup hook | Integration test target | Verify that this hook triggers context loading failure on invalid config. |
| `pom.xml` | Dependencies section | Testing dependencies | Confirmed `spring-boot-starter-test` scope test exists. |

## Confirmed Scope

- Create `BmaquiosquePropertiesValidatorTest.java` in package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config` under `src/test/java`.
- Implement unit tests covering:
  - Blank/null/empty `username` or `password` and check that the validation error "credentials cannot be blank." is returned.
  - Null or negative values for `jitterMinutes` and check that the validation error "jitter-minutes must be a non-negative integer." is returned.
  - Invalid formats for `maxEntryTime` (e.g. `9:00`, `25:00`, `abc`, `12:60`) and check that the validation error "max-entry-time must be in HH:mm format." is returned.
  - Out-of-bounds values for `maxEntryTime` (e.g., before `05:00` or after `22:00`) and check that the validation error "max-entry-time must be between 05:00 and 22:00." is returned.
  - Valid boundary values for `maxEntryTime` (`05:00`, `22:00` and values within) and check that no validation error is returned.
  - Invalid timezone IDs (e.g. `GMT+25`, `Invalid/Timezone`) and check that the validation error "timezone is invalid." is returned.
  - Fully valid configuration properties and check that the errors list is empty.
  - Validate throwing `IllegalArgumentException` when passing null properties bean to validator.
- Create `ConfigurationIntegrationTest.java` in package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config` under `src/test/java`.
- Implement integration tests using `SpringApplicationBuilder` to:
  - Verify that the Spring application context starts successfully when booted with valid configurations.
  - Verify that context loading fails (throws an exception) when booted with invalid configurations (due to `ConfigurationVerificationHook` throwing `IllegalStateException`).
- Run `mvn test` from the workspace root and verify that all test suites execute and pass cleanly.

## Out of Scope

- Testing browser automation, scheduler loops, workday calculation rules (these belong to future feature test suites).
- Modifying main class code (we only write tests).

## Proposed Implementation Approach

1. Create directories `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/` recursively.
2. Implement `BmaquiosquePropertiesValidatorTest` using JUnit 5. Initialize a standard JSR-380 validator in `@BeforeEach` using `Validation.buildDefaultValidatorFactory().getValidator();` and pass it to `BmaquiosquePropertiesValidator`.
3. Implement `ConfigurationIntegrationTest` using `SpringApplicationBuilder` to run test contexts with programmatically injected properties.
4. Run `mvn test` from the root directory to verify that both test suites run and pass.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidatorTest.java` | Create | Confirmed | Task Scope | Unit tests for validator |
| `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationIntegrationTest.java` | Create | Confirmed | Task Scope | Integration tests for context startup |

## Implementation Steps

1. Create directory `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/` if it does not exist.
2. Create `BmaquiosquePropertiesValidatorTest.java` containing unit test cases using JUnit 5 (and parameterized tests where appropriate) to assert validation error messages.
3. Create `ConfigurationIntegrationTest.java` containing integration tests utilizing `SpringApplicationBuilder` to launch/fail-fast Spring Context configurations.
4. Run the command `mvn clean test` from the workspace root directory.
5. Verify that the build succeeds, tests execute, and no failures occur.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| All test cases are implemented using JUnit 5 and Spring Boot Test starters | Implement JUnit 5 unit and Spring Boot integration tests | Successful execution during `mvn test` |
| Test suites run and pass cleanly via `mvn test` | Verify by running `mvn test` at the end | Maven build output with `BUILD SUCCESS` |
| All properties validation constraints and timezone checks have test coverage | Unit tests cover null/blank credentials, negative/null jitter, format and range of max entry time, invalid timezones, and valid properties | Specific assertion checks for each error string |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `mvn clean test` | Command | Run all unit and integration tests to ensure they pass | Verification step of task completion |

## Dependencies

- **Prerequisite**: Task 005 `005-implement-logging-and-masking.md` (completed).
- **Successor**: Task 999 `999-verify-feature-completion.md` (verify feature completion).

## Risks and Edge Cases

- **Spring Context caching / leakage**: Using `try (var context = ...)` properly closes the spring contexts started in programmatic tests, avoiding memory leaks during maven test execution.
- **System Timezone pollution**: The timezone test must verify that the configured timezone string is validated against ZoneId, which is independent of the system's local time zone.

## Rollback or Recovery Notes

- Delete the newly created files under `src/test/java`.

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

- Ensure Jakarta Validation runtime dependency is correctly initialized in the unit test.
- Use programmatic context boot with `SpringApplicationBuilder` to safely test startup failures.
