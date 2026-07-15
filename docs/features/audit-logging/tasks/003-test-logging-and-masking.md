# Task: Test Logback Integration and Masking Converter

## Status

Implemented

## Task ID

TSK-AL-003

## Feature

`docs/features/audit-logging/feature.md`

## Source Documents

- `docs/features/audit-logging/feature.md`
- `docs/features/audit-logging/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create unit tests that validate the `MaskingConverter` regex masking behavior and an integration test that verifies the Spring Boot Logback context initializes without errors.

## Context

The Tech Spec testing strategy defines two required automated test types: unit tests for the masking converter's regex logic and an integration test for Logback configuration validity. This task covers both test types to ensure that credential masking works correctly and that the `logback-spring.xml` configuration is valid.

## Scope

- Create `MaskingConverterTest.java` unit test class.
- Test masking with inputs: `password=mySecret`, `pass: "123"`, `secret=abc`, `credentials=test`, `"password": "xyz"`.
- Assert each input produces the expected masked output (credential value replaced with `******`).
- Test that strings without credential patterns pass through unchanged.
- Create a Spring Boot integration test that bootstraps the application context and verifies the Logback context starts without errors.
- Use JUnit 5 + Spring Boot Test as confirmed in the Technology Definition.

## Out of Scope

- Manual verification of log file rotation (covered in task 999).
- Testing log statements in other application modules.
- Performance benchmarks for the regex.

## Depends On

002-implement-masking-converter.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `MaskingConverterTest.java` exists in the test source tree under the appropriate package.
- Unit tests cover at least the following patterns: `password=mySecret`, `pass: "123"`, `secret=abc`, `credentials=test`.
- Each test asserts that the credential value is replaced with `******`.
- A test verifies that non-sensitive strings pass through unmodified.
- An integration test bootstraps the Spring Boot context and verifies no Logback initialization errors.
- All tests pass with `mvn test`.

## Implementation Notes

- Follow the confirmed stack: JUnit 5 + Spring Boot Test + Mockito as defined in `technology-definition.md`.
- Place unit tests under the test counterpart of `com.lucasbdourado.autotimemarking.shared.infrastructure.logging`.
- Reference Tech Spec § "Testing Strategy" for exact test type requirements and validation targets.
- For the unit test, instantiate `MaskingConverter` directly without Spring context.
- For the integration test, use `@SpringBootTest` to bootstrap the full context.

## Validation Notes

- Run `mvn test` and verify all tests pass.
- Confirm test output shows no Logback configuration warnings or errors.

## Risks

- Integration test may fail if the Spring Boot application context has unrelated configuration issues. The test should focus on Logback initialization, not on other beans.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
- Reference Tech Spec § "Testing Strategy" for the exact test types and what to validate.
