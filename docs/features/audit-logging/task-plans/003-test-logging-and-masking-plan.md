# Task Implementation Plan: Test Logback Integration and Masking Converter

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/audit-logging/task-plans/003-test-logging-and-masking-plan.md`

## Task Reference

Task ID: `TSK-AL-003`

Task file: `docs/features/audit-logging/tasks/003-test-logging-and-masking.md`

Task status: `Depends on Previous Task`

## Feature Reference

Feature name: `audit-logging`

Feature file: `docs/features/audit-logging/feature.md`

Feature Tech Spec: `docs/features/audit-logging/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/audit-logging/tasks/003-test-logging-and-masking.md` | Full document | Confirmed by source document | Defines scope, acceptance criteria, and validation |
| Feature file | `docs/features/audit-logging/feature.md` | Feature Goal, Out of Scope (credentials masking), Risks (credential leakage) | Confirmed by source document | Functional context for testing |
| Feature Tech Spec | `docs/features/audit-logging/tech-spec.md` | § Testing Strategy, § 2 Custom Logback Masking Converter, Validation Rules, Security and Permissions | Confirmed by source document | Primary technical source for test requirements |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions (Testing: JUnit 5 + Spring Boot Test + Mockito, Logging: SLF4J + Logback, Java 21) | Confirmed by source document | Binding stack constraints |
| Package Structure Guidelines | `.agents/docs/architecture/coding-guidelines/package-structure.md` | `shared` usage, package naming conventions | Confirmed by source document | Test file placement mirrors main source |
| Task 002 Plan | `docs/features/audit-logging/task-plans/002-implement-masking-converter-plan.md` | Full document, confirmed regex, mask value | Confirmed by source document | Predecessor task defines the converter being tested |
| MaskingConverter.java | `src/main/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverter.java` | Full class | Detected in codebase | Source under test — actual regex and masking logic |
| logback-spring.xml | `src/main/resources/logback-spring.xml` | conversionRule, LOG_PATTERN | Detected in codebase | Active configuration to validate in integration test |
| BmaquiosquePropertiesValidatorTest.java | `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidatorTest.java` | Testing conventions | Detected in codebase | Establishes test style conventions: package-private classes, AssertJ, `@ParameterizedTest`, `shouldXxxWhenYyy` naming |
| ConfigurationIntegrationTest.java | `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationIntegrationTest.java` | Integration test pattern | Detected in codebase | Establishes integration test conventions: `SpringApplicationBuilder`, `WebApplicationType.NONE`, inner `@SpringBootConfiguration` |
| Integration test placement | User decision in this session | Class name and package | Confirmed by user | `LogbackConfigurationIntegrationTest.java` in `shared.infrastructure.logging` package |
| Test style | User decision in this session | Parameterized vs individual | Confirmed by user | `@ParameterizedTest` with `@MethodSource` for credential patterns; individual `@Test` for non-sensitive and edge cases |

## Planning Scope

This plan covers only TSK-AL-003: creating unit tests for `MaskingConverter` and an integration test for Logback configuration validity. It does not authorize implementation. Feature verification (task 999) is out of scope.

## Task Summary

Create `MaskingConverterTest.java` with parameterized unit tests validating regex-based credential masking across all required patterns, and `LogbackConfigurationIntegrationTest.java` verifying that the Spring Boot application context and Logback configuration initialize without errors.

## Execution Eligibility

Status: Eligible

Reason:

- The dependency task TSK-AL-002 has status `Implemented`.
- `MaskingConverter.java` exists in the codebase with the active regex and masking logic.
- `logback-spring.xml` is fully configured with the `maskedMsg` conversionRule and both appenders using the masked pattern.
- `spring-boot-starter-test` is declared in `pom.xml` with test scope, providing JUnit 5, AssertJ, Mockito, and Spring Boot Test.
- All required source documents are present and confirmed.

## Feature Context

The `audit-logging` feature provides file-based diagnostic logging for the Auto Time Marking application. Credential leakage in log files is identified as a high-impact risk in `feature.md`. The `MaskingConverter` (implemented in task 002) is the primary mitigation. This task validates that mitigation by testing the converter's regex masking logic directly (unit tests) and verifying that the Logback configuration initializes correctly within the Spring Boot context (integration test).

This is the third of four tasks in the audit-logging breakdown. It tests the deliverables from tasks 001 and 002.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| § 1 Spring Boot Integration & SLF4J API | Not applicable | No | SLF4J usage in application code is not part of this task |
| § 2 Custom Logback Masking Converter | Full (test coverage) | Yes — unit tests validate the converter's regex and masking behavior | Tests the regex, mask value, and edge cases |
| § 3 Logback Configuration (`logback-spring.xml`) | Full (test coverage) | Yes — integration test validates configuration bootstrap | Verifies no errors on startup |
| Testing Strategy (Unit: MaskingConverter) | Full | Yes — exact test type defined by Tech Spec | None |
| Testing Strategy (Integration: Logback context) | Full | Yes — exact test type defined by Tech Spec | None |
| Validation Rules (Log Masking) | Full (test coverage) | Yes — tests verify credential values are replaced with `******` | None |
| Security and Permissions (Log Masking) | Full (test coverage) | Yes — tests cover all specified key patterns | None |

Coverage assessment:

- Justifying Tech Spec section: § "Testing Strategy" rows "Unit" and "Integration" directly define the two test types this task must implement.
- Tech Spec sections implemented by this task: Testing Strategy (Unit, Integration), § 2 (test validation), Validation Rules (test validation), Security (test validation).
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| JUnit 5 + Spring Boot Test + Mockito | Technology Definition (confirmed by user) | Test framework for unit and integration tests |
| SLF4J + Logback | Technology Definition (confirmed by user) | Logback classes used in unit test (MessageConverter, ILoggingEvent) and integration test (LoggerContext) |
| Java 21 | Technology Definition (user constraint) | Language features available for test code |
| Spring Boot 3.4.x | Technology Definition (confirmed by user) | `@SpringBootTest` and `SpringApplicationBuilder` for integration test |
| Maven | Technology Definition (confirmed by user) | `mvn test` is the test execution command |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Package Structure | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Test package placement | Test classes mirror the main source package: `shared.infrastructure.logging` |
| Project test conventions | Detected from existing test classes in codebase | Test code style | Package-private classes, AssertJ assertions, `shouldXxxWhenYyy` naming, `@ParameterizedTest` usage |

## Existing Decisions Reviewed

| Decision | Path | Relevance |
| --- | --- | --- |
| MaskingConverter regex pattern | `src/main/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverter.java` (line 12-14) | The actual compiled regex determines expected test outcomes. Actual pattern: `(?i)['"]?(password\|pass\|secret\|credentials?)['"]?\s*[:=]\s*['"]?([^\s'",;]+)['"]?` |
| Mask replacement value `******` | `src/main/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverter.java` (line 16) | Static `MASK = "******"` — test assertions must match this exact value |
| Conversion word `maskedMsg` active | `src/main/resources/logback-spring.xml` (line 3-4) | Integration test verifies this is correctly loaded by Logback |
| LOG_PATTERN uses `%maskedMsg` | `src/main/resources/logback-spring.xml` (line 6) | Integration test verifies configuration is valid |
| Integration test placement | User decision during this planning session | `LogbackConfigurationIntegrationTest.java` in `shared.infrastructure.logging` package |
| Test style for masking patterns | User decision during this planning session | `@ParameterizedTest` with `@MethodSource` for credential patterns; individual `@Test` for non-sensitive and edge cases |

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverter.java` | Full class implementation: regex, convert method, maskCredentialValue method | Direct — source under test | 37 lines. Uses `super.convert(event)` which returns `event.getFormattedMessage()`. Unit tests must mock `ILoggingEvent.getFormattedMessage()`. |
| `src/main/resources/logback-spring.xml` | Full configuration file | Direct — configuration under test | 32 lines. Active conversionRule, both appenders using `${LOG_PATTERN}` with `%maskedMsg`. |
| `src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/` | Existing test classes for conventions | Contextual | 2 test files establish project conventions: package-private, AssertJ, `shouldXxxWhenYyy`, parameterized tests. |
| `src/test/java/com/lucasbdourado/autotimemarking/shared/` | Whether test package exists | Direct | Does not exist. Must be created along with `infrastructure/logging/` sub-packages. |
| `pom.xml` | Test dependency availability | Direct | `spring-boot-starter-test` (scope: test) provides JUnit 5, AssertJ, Mockito, Spring Boot Test. `logback-classic` available transitively via `spring-boot-starter`. No additional dependencies needed. |
| `src/main/java/com/lucasbdourado/autotimemarking/AutoTimeMarkingApplication.java` | Main class and base package | Contextual | Confirms `@SpringBootApplication` and `@ConfigurationPropertiesScan` are present. Integration test context may need to provide required properties or use a minimal `@SpringBootConfiguration`. |

## Confirmed Scope

- Create `MaskingConverterTest.java` in test package `com.lucasbdourado.autotimemarking.shared.infrastructure.logging`.
- Implement parameterized unit tests (`@ParameterizedTest` with `@MethodSource`) covering credential masking for: `password=mySecret`, `pass: "123"`, `secret=abc`, `credentials=test`, `"password": "xyz"`.
- Assert each test case produces the expected masked output with credential values replaced by `******`.
- Implement a `@Test` for non-sensitive strings passing through unchanged.
- Create `LogbackConfigurationIntegrationTest.java` in the same test package.
- Implement an integration test that bootstraps a Spring Boot context and verifies the Logback context starts without errors.
- Use Mockito to mock `ILoggingEvent` in unit tests (stubbing `getFormattedMessage()`).
- Follow project conventions: package-private classes, AssertJ, `shouldXxxWhenYyy` naming.

## Out of Scope

- Manual verification of log file rotation (covered in task 999).
- Testing log statements in other application modules.
- Performance benchmarks for the regex.
- Creating or modifying `MaskingConverter.java` or `logback-spring.xml`.
- Adding new dependencies to `pom.xml`.
- Testing Logback configuration under error conditions (e.g., disk permission failures).

## Proposed Implementation Approach

1. **Create the test package directory** `com.lucasbdourado.autotimemarking.shared.infrastructure.logging` under `src/test/java/`.

2. **Create `MaskingConverterTest.java`**:
   - Package-private class following project conventions.
   - Instantiate `MaskingConverter` directly (no Spring context).
   - Define a `@MethodSource` provider method returning `Stream<Arguments>` with each credential pattern as input and expected output.
   - Create a `@ParameterizedTest` method that mocks `ILoggingEvent`, stubs `getFormattedMessage()` to return the input, calls `converter.convert(event)`, and asserts the result matches the expected masked output using AssertJ.
   - Create a separate `@Test` for non-sensitive strings.
   - Create a separate `@Test` for null/empty message edge cases.

3. **Create `LogbackConfigurationIntegrationTest.java`**:
   - Use `SpringApplicationBuilder` with `WebApplicationType.NONE` (following `ConfigurationIntegrationTest` conventions) or a lightweight `@SpringBootTest`.
   - Provide required properties to avoid startup failures from `ConfigurationVerificationHook` (which validates `bmaquiosque.*` properties).
   - After context bootstrap, access the SLF4J `LoggerFactory` → cast to Logback `LoggerContext` → check `StatusManager` for ERROR-level statuses.
   - Assert no errors exist in the Logback status list.

4. **Run `mvn test`** and verify all tests pass.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/test/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverterTest.java` | Create | Confirmed | Task file, Tech Spec § Testing Strategy, user decision (test style) | Unit test class for MaskingConverter |
| `src/test/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/LogbackConfigurationIntegrationTest.java` | Create | Confirmed | Task file, Tech Spec § Testing Strategy, user decision (placement) | Integration test for Logback context |

## Implementation Steps

1. **Create package directory**: Create `src/test/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/`.

2. **Create `MaskingConverterTest.java`**: Create the file with:
   - Package declaration: `package com.lucasbdourado.autotimemarking.shared.infrastructure.logging;`
   - Imports: `ch.qos.logback.classic.spi.ILoggingEvent`, `org.junit.jupiter.api.Test`, `org.junit.jupiter.params.ParameterizedTest`, `org.junit.jupiter.params.provider.Arguments`, `org.junit.jupiter.params.provider.MethodSource`, `java.util.stream.Stream`, Mockito static imports (`mock`, `when`), AssertJ `assertThat`.
   - Package-private class declaration.
   - Field: `private final MaskingConverter converter = new MaskingConverter();`
   - Static provider method `credentialPatterns()` returning `Stream<Arguments>` with tuples:
     - `("password=mySecret", "password=******")`
     - `("pass: \"123\"", "pass: \"******\"")`
     - `("secret=abc", "secret=******")`
     - `("credentials=test", "credentials=******")`
     - `("\"password\": \"xyz\"", "\"password\": \"******\"")`
   - Parameterized test `shouldMaskCredentialValueWhenPatternMatches(String input, String expected)`:
     - Mock `ILoggingEvent event = mock(ILoggingEvent.class);`
     - Stub: `when(event.getFormattedMessage()).thenReturn(input);`
     - Assert: `assertThat(converter.convert(event)).isEqualTo(expected);`
   - Test `shouldNotMaskWhenNoCredentialPatternIsPresent()`:
     - Input: `"No credentials here, just a normal log message"`
     - Assert output equals input unchanged.
   - Test `shouldReturnNullWhenMessageIsNull()`:
     - Stub `getFormattedMessage()` to return `null`.
     - Assert output is `null`.
   - Test `shouldReturnEmptyWhenMessageIsEmpty()`:
     - Stub `getFormattedMessage()` to return `""`.
     - Assert output is `""`.

3. **Create `LogbackConfigurationIntegrationTest.java`**: Create the file with:
   - Package declaration: `package com.lucasbdourado.autotimemarking.shared.infrastructure.logging;`
   - Imports: `ch.qos.logback.classic.LoggerContext`, `ch.qos.logback.core.status.Status`, `org.junit.jupiter.api.Test`, `org.slf4j.LoggerFactory`, `org.springframework.boot.WebApplicationType`, `org.springframework.boot.builder.SpringApplicationBuilder`, `org.springframework.boot.SpringBootConfiguration`, `org.springframework.context.ConfigurableApplicationContext`, AssertJ `assertThat`.
   - Package-private class declaration.
   - Test `shouldStartLogbackContextWithoutErrors()`:
     - Build context using `SpringApplicationBuilder` with a minimal inner `@SpringBootConfiguration` class, `WebApplicationType.NONE`, and required `bmaquiosque.*` properties to satisfy `ConfigurationVerificationHook`.
     - Use try-with-resources to close the context.
     - Access `LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();`
     - Filter `loggerContext.getStatusManager().getCopyOfStatusList()` for entries with `status.getLevel() == Status.ERROR`.
     - Assert the filtered list is empty.
   - Inner `@SpringBootConfiguration` class providing required beans (following `ConfigurationIntegrationTest` pattern) or using `@SpringBootTest` with property overrides.

     **Note for execute-task:** The existing `ConfigurationIntegrationTest` uses a manual `@SpringBootConfiguration` with explicit bean registration. The integration test here should follow the same pattern to avoid loading the full application context with all modules. Alternatively, if the test only needs to verify Logback initialization (which happens before bean creation), a simpler approach using `@SpringBootTest` with `classes = AutoTimeMarkingApplication.class` and valid properties may suffice — but the manual approach is safer to isolate Logback testing from unrelated context failures, which aligns with the risk noted in the task file.

4. **Run `mvn test`**: Verify all tests pass. Confirm no Logback warnings or errors in test output.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| `MaskingConverterTest.java` exists in the test source tree under the appropriate package | Implementation step 2 creates the file in `shared.infrastructure.logging` test package | File exists at expected path |
| Unit tests cover at least the following patterns: `password=mySecret`, `pass: "123"`, `secret=abc`, `credentials=test` | Implementation step 2 defines all four (plus `"password": "xyz"`) in the `@MethodSource` provider | Parameterized test executes all patterns |
| Each test asserts that the credential value is replaced with `******` | Implementation step 2 — each argument tuple includes expected output with `******` | AssertJ `isEqualTo` assertion in parameterized test |
| A test verifies that non-sensitive strings pass through unmodified | Implementation step 2 — `shouldNotMaskWhenNoCredentialPatternIsPresent()` | AssertJ `isEqualTo` assertion comparing input to output |
| An integration test bootstraps the Spring Boot context and verifies no Logback initialization errors | Implementation step 3 — `shouldStartLogbackContextWithoutErrors()` | AssertJ assertion on Logback StatusManager error list |
| All tests pass with `mvn test` | Implementation step 4 | Clean `mvn test` output |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `shouldMaskCredentialValueWhenPatternMatches` (parameterized) | Unit | Validate MaskingConverter masks each credential pattern correctly | Uses `@ParameterizedTest` with `@MethodSource`. Mocks `ILoggingEvent`. |
| `shouldNotMaskWhenNoCredentialPatternIsPresent` | Unit | Validate passthrough of non-sensitive strings | Individual `@Test`. |
| `shouldReturnNullWhenMessageIsNull` | Unit | Validate null safety | Individual `@Test`. Edge case coverage. |
| `shouldReturnEmptyWhenMessageIsEmpty` | Unit | Validate empty string handling | Individual `@Test`. Edge case coverage. |
| `shouldStartLogbackContextWithoutErrors` | Integration | Validate `logback-spring.xml` with MaskingConverter initializes without errors | Uses `SpringApplicationBuilder` with lightweight context. Checks Logback `StatusManager`. |
| `mvn test` | Build | Verify all tests pass and no Logback warnings | Final validation command |

## Dependencies

- **TSK-AL-002** (`002-implement-masking-converter`): Status `Implemented`. `MaskingConverter.java` and active `logback-spring.xml` configuration exist. This dependency is satisfied.
- **TSK-AL-001** (`001-configure-logback-appenders`): Status `Implemented`. `logback-spring.xml` with console and rolling file appenders exists. Transitively satisfied via TSK-AL-002.
- **Runtime dependency**: `spring-boot-starter-test` in `pom.xml` provides JUnit 5, AssertJ, Mockito, and Spring Boot Test. `ch.qos.logback:logback-classic` is available transitively. No additional dependencies needed.
- **No external system dependencies**.

## Risks and Edge Cases

- **Integration test may fail from unrelated context issues**: The task file explicitly identifies this risk. Mitigation: use a minimal `@SpringBootConfiguration` inner class (following `ConfigurationIntegrationTest` pattern) instead of loading the full application context. This isolates Logback initialization testing from unrelated bean creation failures.
- **`ConfigurationVerificationHook` requires valid `bmaquiosque.*` properties**: If the integration test loads the full application context, it must provide valid properties. If using a minimal inner configuration, this bean can be omitted. The execute-task agent should verify which approach avoids false failures.
- **LoggerContext cast safety**: `LoggerFactory.getILoggerFactory()` returns `ILoggerFactory`. In a Spring Boot/Logback environment, this is always a `LoggerContext`. If the test environment changes the logging backend, this cast would fail. Risk is negligible for this project.
- **Mockito mocking `ILoggingEvent`**: The `convert` method calls `super.convert(event)`, which internally calls `event.getFormattedMessage()`. Mocking `ILoggingEvent` with Mockito and stubbing `getFormattedMessage()` is the standard approach. However, `MessageConverter.convert()` may also call other methods on `ILoggingEvent` — the execute-task agent should verify with a quick test run.
- **Regex edge cases beyond required patterns**: The task scope covers only the listed patterns. Additional edge cases (multi-line, multiple credentials in one string, embedded patterns) are not required but may be added as bonus coverage during execution.

## Rollback or Recovery Notes

- **Delete test files**: Remove `MaskingConverterTest.java` and `LogbackConfigurationIntegrationTest.java`.
- **Remove empty directories**: Delete `src/test/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/` if no other content exists.
- No production code or configuration is modified by this task.

## Pending Decisions

```text
None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.
```

## Questions for the User

```text
None. All task-relevant questions have been answered.
```

## Decisions Created During Planning

```text
No local feature/task decisions were created during this planning session.
```

The user-confirmed decisions (integration test placement in `shared.infrastructure.logging` and `@ParameterizedTest` with `@MethodSource` test style) are direct implementation instructions documented in this plan, not standalone decision files, because they follow naturally from the Tech Spec, project conventions, and task scope.

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

- Read `MaskingConverter.java` before writing unit tests — the actual regex pattern includes optional quotes around key names (`['"]?` before and after the key group), which differs slightly from the Tech Spec's original regex. Test expectations must match the implemented regex behavior.
- The `convert` method calls `super.convert(event)` which delegates to `event.getFormattedMessage()`. Mock `ILoggingEvent` with Mockito and stub `getFormattedMessage()`.
- Use `Mockito.mock(ILoggingEvent.class)` — do not use `@Mock` annotation to avoid needing `@ExtendWith(MockitoExtension.class)` for a simple test.
- Follow project conventions: package-private class, AssertJ assertions, `shouldXxxWhenYyy` naming.
- The `credentialPatterns()` method source must include all five patterns listed in the task scope plus the expected masked output for each.
- For the integration test, use a minimal `@SpringBootConfiguration` inner class (following `ConfigurationIntegrationTest` pattern) to avoid loading the full application context. Provide valid `bmaquiosque.*` properties if the configuration beans are loaded, or exclude them if using a minimal context that only tests Logback initialization.
- Check `LoggerContext.getStatusManager().getCopyOfStatusList()` and filter for `Status.ERROR` level entries. Do not assert on WARN or INFO statuses — those may be normal Logback output.
- After implementation, run `mvn test` and verify all tests pass. Confirm test output shows no Logback configuration warnings or errors.
- Do not modify production code, `MaskingConverter.java`, `logback-spring.xml`, or `pom.xml`.
