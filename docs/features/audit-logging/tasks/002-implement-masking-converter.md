# Task: Implement Custom Logback Masking Converter

## Status

Depends on Previous Task

## Task ID

TSK-AL-002

## Feature

`docs/features/audit-logging/feature.md`

## Source Documents

- `docs/features/audit-logging/feature.md`
- `docs/features/audit-logging/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `MaskingConverter` class that extends Logback's `MessageConverter` to intercept and mask sensitive credential values in log messages, and register it in `logback-spring.xml`.

## Context

The feature.md identifies credential leakage in log files as a high-impact risk. The Tech Spec defines a custom Logback converter that uses regex to detect and replace credential values (password, pass, secret, credentials) with `******` before they reach log appenders. This converter must be placed in `shared.infrastructure.logging` per the coding guidelines package structure.

## Scope

- Create `MaskingConverter.java` in package `com.lucasbdourado.autotimemarking.shared.infrastructure.logging`.
- Extend `ch.qos.logback.classic.pattern.MessageConverter`.
- Override the `convert` method to apply regex-based masking.
- Use the confirmed regex pattern: `(?i)(password|pass|secret|credentials?)\s*[:=]\s*['"]?([^\s'",;]+)['"]?`
- Replace matched credential values with `******`.
- Register the converter in `logback-spring.xml` using a `<conversionRule>` element.
- Update the encoder patterns in both Console and Rolling File appenders to use the custom conversion word.

## Out of Scope

- Creating the `logback-spring.xml` file from scratch (done in task 001).
- Writing unit tests for the converter (task 003).
- Adding log statements to application modules.
- Masking patterns beyond the confirmed regex (e.g., credit card numbers, tokens).

## Depends On

001-configure-logback-appenders.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `MaskingConverter.java` exists in package `com.lucasbdourado.autotimemarking.shared.infrastructure.logging`.
- The class extends `ch.qos.logback.classic.pattern.MessageConverter`.
- The `convert` method applies the confirmed regex pattern and replaces matched credential values with `******`.
- The converter handles inputs like `password=mySecret`, `"password": "xyz"`, `pass=abc`, `secret=123`, `credentials=test` and masks them.
- The converter is registered in `logback-spring.xml` via `<conversionRule>`.
- Both Console and Rolling File appender patterns use the masking conversion word.
- Spring Boot application starts without errors with the updated configuration.

## Implementation Notes

- Follow the confirmed stack and constraints from `docs/architecture/auto-time-marking/technology-definition.md`.
- Place the class under `shared.infrastructure.logging` per the package structure guidelines in `.agents/docs/architecture/coding-guidelines/package-structure.md`.
- Reference Tech Spec § "Custom Logback Masking Converter" for the exact class name, package, and regex.
- Reference Tech Spec § "Validation Rules" for enforcement expectations.
- Keep the regex simple and optimized to avoid CPU overhead (Tech Spec § Risks).

## Validation Notes

- Run `mvn clean compile` to check compilation.
- Manually verify that a log statement containing `password=secret123` outputs `password=******` in both console and log file.

## Risks

- Regex performance overhead if patterns are too complex. Keep the regex simple as defined in the Tech Spec.
- Logger masking bypass if developers construct log strings with bare passwords outside SLF4J placeholders (Tech Spec risk, status Open).

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
- Reference Tech Spec § "Proposed Technical Approach" section 2 for the converter implementation details.
