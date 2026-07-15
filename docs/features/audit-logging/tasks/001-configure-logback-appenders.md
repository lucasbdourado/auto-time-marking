# Task: Configure Logback Console and Rolling File Appenders

## Status

Implemented

## Task ID

TSK-AL-001

## Feature

`docs/features/audit-logging/feature.md`

## Source Documents

- `docs/features/audit-logging/feature.md`
- `docs/features/audit-logging/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Create the `logback-spring.xml` configuration file under `src/main/resources/` with a Console appender and a Rolling File appender, applying the confirmed log format pattern, rotation policy, and file storage paths.

## Context

The audit-logging feature requires a file-based logging infrastructure for the Auto Time Marking application. Spring Boot 3.4.x uses SLF4J + Logback as its default logging system. The Tech Spec defines the exact log pattern, file paths, rotation limits, and appender structure. This task creates the foundational logging configuration that all other modules will use.

## Scope

- Create `src/main/resources/logback-spring.xml`.
- Define a `CONSOLE` appender writing to stdout.
- Define a `ROLLING_FILE` appender writing to `logs/auto-time-marking.log`.
- Apply the confirmed log format pattern: `[Timestamp] [Thread] [Level] [Logger] - Message`.
- Configure `SizeAndTimeBasedRollingPolicy` with:
  - Rolled files pattern: `logs/archived/auto-time-marking-%d{yyyy-MM-dd}.%i.log`
  - Maximum file size: `10MB`
  - Maximum history: `5` files
  - Total cap: `50MB`
- Set root logger level to `INFO` and attach both appenders.
- Include a placeholder `conversionRule` element for the masking converter (to be implemented in task 002).

## Out of Scope

- Implementing the `MaskingConverter` Java class (task 002).
- Writing tests (task 003).
- Changing application code to add log statements.
- Configuring async appenders (not required for MVP per Tech Spec performance considerations).

## Depends On

None

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `src/main/resources/logback-spring.xml` exists and is well-formed XML.
- A `CONSOLE` appender is defined and writes to stdout.
- A `ROLLING_FILE` appender is defined and writes to `logs/auto-time-marking.log`.
- Log format pattern matches `[Timestamp] [Thread] [Level] [Logger] - Message`.
- Rolling policy uses `SizeAndTimeBasedRollingPolicy` with 10MB max file size, 5 max history, and 50MB total cap.
- Rolled files follow the pattern `logs/archived/auto-time-marking-%d{yyyy-MM-dd}.%i.log`.
- Root logger is set to `INFO` level with both appenders attached.
- Spring Boot application starts without Logback configuration errors.

## Implementation Notes

- Follow the confirmed stack: SLF4J + Logback as defined in `technology-definition.md`.
- Use `logback-spring.xml` (not `logback.xml`) to leverage Spring Boot profile-aware configuration (Tech Spec § 3).
- Reference Tech Spec § "Logback Configuration (`logback-spring.xml`)" for exact appender definitions, pattern, and rotation parameters.
- Reference coding guidelines in `.agents/docs/architecture/coding-guidelines/` for package and file placement conventions.

## Validation Notes

- Run `mvn clean compile` to verify `logback-spring.xml` is valid.
- Start the Spring Boot application and verify that both console output and `logs/auto-time-marking.log` file are created with the correct format.

## Risks

- Incorrect XML syntax could prevent Spring Boot from starting. Validate XML structure.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
- Reference Tech Spec § "Proposed Technical Approach" section 3 for the exact Logback configuration details.
