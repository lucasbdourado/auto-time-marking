# Task Implementation Plan: Implement Custom Logback Masking Converter

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/audit-logging/task-plans/002-implement-masking-converter-plan.md`

## Task Reference

Task ID: `TSK-AL-002`

Task file: `docs/features/audit-logging/tasks/002-implement-masking-converter.md`

Task status: `Depends on Previous Task`

## Feature Reference

Feature name: `audit-logging`

Feature file: `docs/features/audit-logging/feature.md`

Feature Tech Spec: `docs/features/audit-logging/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/audit-logging/tasks/002-implement-masking-converter.md` | Full document | Confirmed by source document | Defines scope, acceptance criteria, and validation |
| Feature file | `docs/features/audit-logging/feature.md` | Feature Goal, Scope, Out of Scope (credentials masking), Risks (credential leakage) | Confirmed by source document | Functional context for credential protection |
| Feature Tech Spec | `docs/features/audit-logging/tech-spec.md` | § 2 Custom Logback Masking Converter, Confirmed Technology Decisions, Validation Rules, Security and Permissions, Testing Strategy, Architecture Notes | Confirmed by source document | Primary technical source for converter implementation |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions (Logging: SLF4J + Logback, Java 21, Spring Boot 3.4.x) | Confirmed by source document | Binding stack constraints |
| Package Structure Guidelines | `.agents/docs/architecture/coding-guidelines/package-structure.md` | `shared` usage, package naming conventions | Confirmed by source document | File placement for shared infrastructure |
| Infrastructure Layer Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Infrastructure responsibilities, no domain rules in infrastructure | Confirmed by source document | Confirms converter is an infrastructure concern |
| Task 001 Plan | `docs/features/audit-logging/task-plans/001-configure-logback-appenders-plan.md` | MaskingConverter placeholder, conversion word `maskedMsg` | Confirmed by source document | Predecessor task output defines integration point |
| Logback configuration | `src/main/resources/logback-spring.xml` | Lines 3-5 (commented conversionRule), line 7 (LOG_PATTERN) | Detected in codebase | Current state after task 001 execution |

## Planning Scope

This plan covers only TSK-AL-002: creating the `MaskingConverter` Java class and updating `logback-spring.xml` to activate the converter. It does not authorize implementation. Unit tests (task 003) and feature verification (task 999) are out of scope.

## Task Summary

Create `MaskingConverter.java` in package `com.lucasbdourado.autotimemarking.shared.infrastructure.logging` that extends Logback's `MessageConverter`, overrides the `convert` method to apply regex-based credential masking, and then update `logback-spring.xml` to uncomment the `conversionRule` and replace `%msg` with `%maskedMsg` in the shared log pattern.

## Execution Eligibility

Status: Eligible

Reason:

- The dependency task TSK-AL-001 has status `Implemented`.
- `logback-spring.xml` already exists with the placeholder `conversionRule` (commented out) and conversion word `maskedMsg`.
- All required source documents are present and confirmed.
- All technology decisions are confirmed.
- The `shared/infrastructure/logging` package does not exist yet and will be created.

## Feature Context

The `audit-logging` feature provides file-based diagnostic logging for the Auto Time Marking application. The feature.md identifies credential leakage in log files as a high-impact risk. The masking converter is the primary mitigation: it intercepts every log message at the Logback formatting layer and replaces credential values with `******` before they reach console or file output.

This task is the second of four in the audit-logging breakdown. It builds on the Logback configuration created in task 001 and will be tested by task 003.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| § 1 Spring Boot Integration & SLF4J API | Not applicable | No | SLF4J usage in application code is not part of this task |
| § 2 Custom Logback Masking Converter | Full | Yes — class name, package, superclass, regex, and masking behavior | None |
| § 3 Logback Configuration (`logback-spring.xml`) | Partial | Yes — uncomment `conversionRule`, update `LOG_PATTERN` to use `%maskedMsg` | Console/rolling appender structure already created by task 001 |
| Confirmed Technology Decisions (Protection: Regex Masking Converter) | Full | Yes | None |
| Validation Rules (Log Masking) | Full | Yes — `MaskingConverter` execution replaces credential values with `******` | None |
| Security and Permissions (Log Masking) | Full | Yes — masks values for keys matching password, pass, secret, credential | None |
| Testing Strategy (Unit: MaskingConverter) | Not applicable | No — tests are task 003 | None |
| Architecture Notes (Converter node) | Full | Yes — implements the Converter node in the architecture diagram | None |
| Performance Considerations | Full | Yes — simple regex, synchronous execution acceptable for MVP | None |

Coverage assessment:

- Justifying Tech Spec section: § 2 "Custom Logback Masking Converter" directly defines the converter class, package, superclass, and regex.
- Tech Spec sections implemented by this task: § 2 (full), § 3 (partial — conversionRule and pattern update), Validation Rules (full), Security (full).
- Gaps between task and Tech Spec: None for in-scope items. Unit tests are correctly deferred to task 003.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| SLF4J + Logback | Technology Definition (confirmed by user) | Converter extends `ch.qos.logback.classic.pattern.MessageConverter` from Logback |
| Java 21 | Technology Definition (user constraint) | Determines language features available (can use text blocks, records, etc. but not needed here) |
| Spring Boot 3.4.x | Technology Definition (confirmed by user) | Logback is transitively provided by `spring-boot-starter`; no additional dependency needed |
| Maven | Technology Definition (confirmed by user) | `spring-boot-starter` in `pom.xml` provides Logback transitively |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Package Structure | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Package placement | Class goes under `shared/infrastructure/logging`. The `shared` package is appropriate because the MaskingConverter is a cross-cutting infrastructure concern, not module-specific. |
| Infrastructure Layer | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Layer responsibilities | Confirms the converter is an infrastructure component. It must not contain domain rules — it only applies technical regex masking. |

## Existing Decisions Reviewed

| Decision | Path | Relevance |
| --- | --- | --- |
| Conversion word `maskedMsg` | `src/main/resources/logback-spring.xml` (line 4) | Established by task 001. The `conversionRule` placeholder uses `conversionWord="maskedMsg"`. This task must use the same word. |
| Pattern replacement `%msg` → `%maskedMsg` | User decision during this planning session | Confirmed by user: replace `%msg` with `%maskedMsg` in the shared `LOG_PATTERN` property so both appenders apply masking. |

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/resources/logback-spring.xml` | Current file content after task 001 execution | Direct | Lines 3-5: commented `conversionRule` with `maskedMsg` and FQCN. Line 7: `LOG_PATTERN` property with `%msg`. Both need modification. |
| `src/main/java/com/lucasbdourado/autotimemarking/` | Whether `shared/` package exists | Direct | Does not exist. Must be created along with `infrastructure/logging/` sub-packages. |
| `pom.xml` | Whether Logback is available | Direct | `spring-boot-starter` is present. `ch.qos.logback.classic.pattern.MessageConverter` is available without additional dependencies. |
| `src/main/java/com/lucasbdourado/autotimemarking/AutoTimeMarkingApplication.java` | Project main class and base package | Contextual | Confirms base package is `com.lucasbdourado.autotimemarking`. |

## Confirmed Scope

- Create `MaskingConverter.java` in package `com.lucasbdourado.autotimemarking.shared.infrastructure.logging`.
- Extend `ch.qos.logback.classic.pattern.MessageConverter`.
- Override the `convert` method to apply regex-based masking.
- Use the confirmed regex pattern: `(?i)(password|pass|secret|credentials?)\s*[:=]\s*['"]?([^\s'",;]+)['"]?`.
- Replace matched credential values (capture group 2) with `******`.
- Compile the regex pattern as a static `java.util.regex.Pattern` constant for performance.
- Uncomment the `conversionRule` element in `logback-spring.xml`.
- Update the `LOG_PATTERN` property in `logback-spring.xml` to use `%maskedMsg` instead of `%msg`.

## Out of Scope

- Creating `logback-spring.xml` from scratch (already done in task 001).
- Writing unit tests for the converter (task 003).
- Adding log statements to application modules.
- Masking patterns beyond the confirmed regex (e.g., credit card numbers, tokens, API keys).
- Async or buffered masking strategies.
- Creating the `logs/` directory (handled by Logback).

## Proposed Implementation Approach

1. **Create the package directory** `com.lucasbdourado.autotimemarking.shared.infrastructure.logging` under `src/main/java/`.

2. **Create `MaskingConverter.java`**:
   - Declare the class extending `ch.qos.logback.classic.pattern.MessageConverter`.
   - Define a `private static final Pattern` constant with the compiled regex `(?i)(password|pass|secret|credentials?)\s*[:=]\s*['"]?([^\s'",;]+)['"]?`.
   - Define a `private static final String` constant `MASK = "******"` for the replacement value.
   - Override `public String convert(ILoggingEvent event)`:
     - Call `super.convert(event)` to get the original formatted message.
     - Apply `Matcher.replaceAll` using the compiled pattern to replace capture group 2 with `MASK` while preserving the key and separator (capture group 1 and the separator characters).
     - Return the masked message.

3. **Update `logback-spring.xml`**:
   - Uncomment the `conversionRule` element (lines 3-5) to activate the `maskedMsg` conversion word.
   - Update the `LOG_PATTERN` property value from `%msg` to `%maskedMsg`.

4. **Validate**: Compile with `mvn clean compile` and verify Spring Boot starts without errors.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/MaskingConverter.java` | Create | Confirmed | Task file, Tech Spec § 2, Package Structure Guidelines | Primary deliverable |
| `src/main/resources/logback-spring.xml` | Modify | Confirmed | Task file (scope item), codebase (current state) | Uncomment conversionRule, update LOG_PATTERN |

## Implementation Steps

1. **Create package directory**: Create `src/main/java/com/lucasbdourado/autotimemarking/shared/infrastructure/logging/`.

2. **Create `MaskingConverter.java`**: Create the file with:
   - Package declaration: `package com.lucasbdourado.autotimemarking.shared.infrastructure.logging;`
   - Imports: `ch.qos.logback.classic.pattern.MessageConverter`, `ch.qos.logback.classic.spi.ILoggingEvent`, `java.util.regex.Matcher`, `java.util.regex.Pattern`.
   - Class declaration extending `MessageConverter`.
   - Static compiled pattern:
     ```java
     private static final Pattern CREDENTIAL_PATTERN = Pattern.compile(
         "(?i)(password|pass|secret|credentials?)\\s*[:=]\\s*['\"]?([^\\s'\",;]+)['\"]?"
     );
     ```
   - Static mask constant:
     ```java
     private static final String MASK = "******";
     ```
   - Override `convert` method:
     ```java
     @Override
     public String convert(ILoggingEvent event) {
         String message = super.convert(event);
         if (message == null || message.isEmpty()) {
             return message;
         }
         Matcher matcher = CREDENTIAL_PATTERN.matcher(message);
         return matcher.replaceAll(matchResult -> {
             String prefix = message.substring(matchResult.start(), matchResult.start(2));
             return Matcher.quoteReplacement(prefix + MASK);
         });
     }
     ```
   - Note: The `replaceAll` with a lambda reconstructs the key+separator prefix and appends `MASK`, discarding the original credential value. `Matcher.quoteReplacement` prevents special characters in the prefix from being interpreted as replacement references.

3. **Uncomment `conversionRule` in `logback-spring.xml`**: Change lines 3-5 from:
   ```xml
   <!-- TODO: Activate after MaskingConverter is implemented in task 002 -->
   <!-- <conversionRule conversionWord="maskedMsg"
        converterClass="com.lucasbdourado.autotimemarking.shared.infrastructure.logging.MaskingConverter" /> -->
   ```
   To:
   ```xml
   <conversionRule conversionWord="maskedMsg"
        converterClass="com.lucasbdourado.autotimemarking.shared.infrastructure.logging.MaskingConverter" />
   ```

4. **Update `LOG_PATTERN` in `logback-spring.xml`**: Change:
   ```xml
   <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n" />
   ```
   To:
   ```xml
   <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %maskedMsg%n" />
   ```

5. **Compile and validate**: Run `mvn clean compile` to verify compilation. Start the application to verify Logback initializes successfully with the active `MaskingConverter`.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| `MaskingConverter.java` exists in package `com.lucasbdourado.autotimemarking.shared.infrastructure.logging` | Implementation step 2 creates the file in the correct package | File exists at expected path after implementation |
| The class extends `ch.qos.logback.classic.pattern.MessageConverter` | Implementation step 2 declares the class extending `MessageConverter` | Class declaration in source code |
| The `convert` method applies the confirmed regex pattern and replaces matched credential values with `******` | Implementation step 2 implements `convert` with the confirmed regex and `MASK` constant | Source code review; formal unit tests in task 003 |
| The converter handles inputs like `password=mySecret`, `"password": "xyz"`, `pass=abc`, `secret=123`, `credentials=test` and masks them | Implementation step 2 — regex pattern covers all listed key variants with `[:=]` separator and optional quotes | Formal unit tests in task 003; manual verification during task 002 validation |
| The converter is registered in `logback-spring.xml` via `<conversionRule>` | Implementation step 3 uncomments the `conversionRule` element | XML file inspection |
| Both Console and Rolling File appender patterns use the masking conversion word | Implementation step 4 updates the shared `LOG_PATTERN` from `%msg` to `%maskedMsg` | XML file inspection; both appenders reference `${LOG_PATTERN}` |
| Spring Boot application starts without errors with the updated configuration | Implementation step 5 validates startup | `mvn clean compile` succeeds; application starts without Logback errors |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `mvn clean compile` | Build | Verify the Java class compiles and the updated XML is valid | Quick feedback on compilation |
| Spring Boot application start | Manual | Verify Logback initializes the `MaskingConverter` without errors | Run main class or `mvn spring-boot:run` |
| Manual log statement with credential | Manual | Log a message containing `password=secret123` and verify console/file output shows `password=******` | Validates end-to-end masking behavior |
| Formal unit tests | Unit (deferred to task 003) | Validate all regex variants systematically | Not part of this task |

## Dependencies

- **TSK-AL-001** (`001-configure-logback-appenders`): Status `Implemented`. The `logback-spring.xml` file with the placeholder `conversionRule` already exists. This dependency is satisfied.
- **Runtime dependency**: `spring-boot-starter` in `pom.xml` provides Logback transitively. `ch.qos.logback.classic.pattern.MessageConverter` is available without additional dependencies.
- **No external system dependencies**.

## Risks and Edge Cases

- **Regex performance overhead**: The regex is simple and compiled statically. For a single-user MVP waking every 30 minutes, the overhead is negligible. Tech Spec explicitly accepts this (§ Risks: "Mitigated").
- **Regex false positives**: The pattern is intentionally broad to catch common credential key names. A log message like `"The password policy requires 8 characters"` would not match because there is no `[:=]` separator followed by a value. However, a message like `"Set password=<policy-name>"` would mask `<policy-name>`. This is acceptable for the MVP; the Tech Spec treats broader masking concerns as an Open risk (logger masking bypass).
- **Null or empty messages**: The `convert` method handles null and empty messages by returning them unchanged.
- **Special regex characters in credential values**: The `replaceAll` lambda uses `Matcher.quoteReplacement` to prevent special characters in the reconstructed prefix from causing replacement errors.
- **Conversion word mismatch**: If the `conversionWord` in the XML does not match the `%maskedMsg` token in the pattern, masking will silently not apply. Both are confirmed to use `maskedMsg`.

## Rollback or Recovery Notes

- **Rollback `logback-spring.xml`**: Re-comment the `conversionRule` element and revert `%maskedMsg` to `%msg` in the `LOG_PATTERN`. Spring Boot will revert to standard unmasked logging.
- **Delete `MaskingConverter.java`**: Remove the file and its parent directories (`shared/infrastructure/logging/`) if no other content exists there.
- No other files are modified by this task.

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

The user-confirmed decision to replace `%msg` with `%maskedMsg` in the shared `LOG_PATTERN` is a direct implementation instruction documented in this plan, not a standalone decision file, because it follows naturally from the Tech Spec and task scope.

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

- Read Tech Spec § 2 "Custom Logback Masking Converter" before implementing the Java class.
- The regex replacement must preserve the key name and separator (e.g., `password=`) while replacing only the credential value. Do not mask the entire match including the key.
- The `convert` method must call `super.convert(event)` first to get the formatted message string, then apply regex masking on the result.
- Use a static compiled `Pattern` — do not recompile the regex on every invocation.
- When updating `logback-spring.xml`, remove the `<!-- TODO -->` comment along with the XML comment wrapping the `conversionRule`.
- The `LOG_PATTERN` property change from `%msg` to `%maskedMsg` applies to both appenders automatically because both encoders reference `${LOG_PATTERN}`.
- After implementation, run `mvn clean compile` and start the application. Verify that a log message containing `password=secret123` outputs `password=******` in both console and the `logs/auto-time-marking.log` file.
- Do not write unit tests — those belong to task 003.
- Do not add log statements to application modules — that is out of scope.
