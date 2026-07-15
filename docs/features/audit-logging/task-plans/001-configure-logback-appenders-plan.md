# Task Implementation Plan: Configure Logback Console and Rolling File Appenders

## Status

Status: Ready for Implementation

Last updated: 2026-07-14

Plan file: `docs/features/audit-logging/task-plans/001-configure-logback-appenders-plan.md`

## Task Reference

Task ID: `TSK-AL-001`

Task file: `docs/features/audit-logging/tasks/001-configure-logback-appenders.md`

Task status: `Ready`

## Feature Reference

Feature name: `audit-logging`

Feature file: `docs/features/audit-logging/feature.md`

Feature Tech Spec: `docs/features/audit-logging/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/audit-logging/tasks/001-configure-logback-appenders.md` | Full document | Confirmed by source document | Defines scope, acceptance criteria, and validation |
| Feature file | `docs/features/audit-logging/feature.md` | Feature Goal, Scope, Expected Outcome, Risks | Confirmed by source document | Functional context for logging infrastructure |
| Feature Tech Spec | `docs/features/audit-logging/tech-spec.md` | § 3 Logback Configuration, Confirmed Technology Decisions, Architecture Notes, Performance Considerations | Confirmed by source document | Primary technical source for exact appender definitions and parameters |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions (Logging: SLF4J + Logback), Inputs for Tech Spec | Confirmed by source document | Binding stack constraints |
| Tasks README | `docs/features/audit-logging/tasks/README.md` | Task List, Dependency Notes | Confirmed by source document | Task sequencing context |
| Package Structure Guidelines | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Full document | Confirmed by source document | File placement conventions |
| Infrastructure Layer Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Full document | Confirmed by source document | Infrastructure layer responsibilities |

## Planning Scope

This plan covers only TSK-AL-001: creating the `logback-spring.xml` configuration file with Console and Rolling File appenders. It does not authorize implementation. The MaskingConverter class (task 002), tests (task 003), and feature verification (task 999) are out of scope.

## Task Summary

Create a well-formed `src/main/resources/logback-spring.xml` file that configures a `CONSOLE` appender (stdout), a `ROLLING_FILE` appender with `SizeAndTimeBasedRollingPolicy`, the confirmed log format pattern, and a placeholder `conversionRule` for the future MaskingConverter.

## Execution Eligibility

Status: Eligible

Reason:
- This task has no dependencies. It is the first task in the audit-logging feature breakdown.
- All required source documents are present and confirmed.
- All technology decisions are confirmed.
- The `src/main/resources/` directory exists and no conflicting `logback-spring.xml` file is present.

## Feature Context

The `audit-logging` feature provides file-based diagnostic logging for the Auto Time Marking application. In the MVP, log files are the primary observability mechanism (no database, no external log aggregator, no UI). The feature requires:
1. A logging infrastructure with console and rolling file appenders (this task).
2. A masking converter to prevent credential leaks (task 002).
3. Tests to validate both (task 003).

This task establishes the foundational Logback configuration that all subsequent logging work depends on.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| § 1 Spring Boot Integration & SLF4J API | Not applicable | No | This task creates configuration only, not Java code using SLF4J |
| § 2 Custom Logback Masking Converter | Partial | Placeholder `conversionRule` element only | Full MaskingConverter implementation is task 002 |
| § 3 Logback Configuration (`logback-spring.xml`) | Full | Yes — Console appender, Rolling File appender, pattern, rotation policy, root logger | None |
| Confirmed Technology Decisions | Full | Yes — uses SLF4J + Logback as confirmed | None |
| Architecture Notes | Partial | Yes — creates the Console and File Appender nodes shown in the architecture diagram | MaskingConverter node is task 002 |
| Performance Considerations | Full | Yes — synchronous appenders as specified for MVP | Async appenders explicitly excluded |

Coverage assessment:

- Justifying Tech Spec section: § 3 "Logback Configuration (`logback-spring.xml`)" directly defines the content of this task.
- Tech Spec sections implemented by this task: § 3 (full), § 2 (placeholder only), Architecture Notes (partial).
- Gaps between task and Tech Spec: None for in-scope items. The MaskingConverter class is correctly deferred to task 002.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| SLF4J + Logback | Technology Definition (confirmed by user) | Logback is the logging provider; `logback-spring.xml` is the standard configuration file |
| Spring Boot 3.4.x | Technology Definition (confirmed by user) | Use `logback-spring.xml` (not `logback.xml`) to leverage Spring Boot profile-aware configuration |
| Java 21 | Technology Definition (user constraint) | No direct impact on XML configuration; determines runtime compatibility |
| Maven | Technology Definition (confirmed by user) | `spring-boot-starter` in `pom.xml` transitively includes Logback; no additional dependency needed |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Package Structure | `.agents/docs/architecture/coding-guidelines/package-structure.md` | File placement | Confirms `logback-spring.xml` placement in `src/main/resources/` (standard Spring Boot convention). The `shared/infrastructure/logging` package structure is relevant for the MaskingConverter in task 002, not for this XML file. |
| Infrastructure Layer | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Infrastructure concerns | Logging configuration is an infrastructure concern. The guideline confirms that infrastructure handles framework-level configuration. No domain rules should be placed in the logging config. |

## Existing Decisions Reviewed

```text
No existing feature, ADR, or architecture decision was relevant to this task.
```

The `docs/features/audit-logging/decisions/` directory does not exist yet. No ADRs exist in the project. All relevant technology decisions are captured in the Technology Definition.

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/resources/` | Whether `logback-spring.xml` already exists | Direct | File does not exist — will be created. No conflict. |
| `src/main/resources/application.properties` | Whether any `logging.*` properties are configured | Direct | No logging-related properties found. No conflict with Logback XML config. |
| `pom.xml` | Whether `spring-boot-starter` is present (transitively includes Logback) | Direct | `spring-boot-starter` is present. Logback is available without additional dependencies. |

## Confirmed Scope

- Create `src/main/resources/logback-spring.xml`.
- Define a `CONSOLE` appender writing to stdout using `ch.qos.logback.core.ConsoleAppender`.
- Define a `ROLLING_FILE` appender using `ch.qos.logback.core.rolling.RollingFileAppender` writing to `logs/auto-time-marking.log`.
- Apply the confirmed log format pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n` (maps to `[Timestamp] [Thread] [Level] [Logger] - Message`).
- Configure `SizeAndTimeBasedRollingPolicy` with:
  - Rolled files pattern: `logs/archived/auto-time-marking-%d{yyyy-MM-dd}.%i.log`
  - Maximum file size: `10MB`
  - Maximum history: `5`
  - Total cap: `50MB`
- Set root logger level to `INFO` and attach both appenders.
- Include a placeholder `conversionRule` element for `MaskingConverter` with the future fully-qualified class name `com.lucasbdourado.autotimemarking.shared.infrastructure.logging.MaskingConverter` and a conversion word (e.g., `maskedMsg`). The placeholder will be commented out so that Spring Boot starts without errors.

## Out of Scope

- Implementing the `MaskingConverter` Java class (task 002).
- Writing unit or integration tests (task 003).
- Adding log statements to application code.
- Configuring async appenders (not required for MVP per Tech Spec).
- Adding `logging.*` properties in `application.properties` (Logback XML takes full control).
- Creating the `logs/` directory manually (Logback creates it automatically at runtime).

## Proposed Implementation Approach

1. Create the file `src/main/resources/logback-spring.xml` with proper XML declaration and `<configuration>` root element.
2. Add a commented-out `<conversionRule>` element for the future `MaskingConverter` with an XML comment explaining it will be activated in task 002.
3. Define a `<property>` element for the log pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`.
4. Define the `CONSOLE` appender as a `ConsoleAppender` using the shared pattern property.
5. Define the `ROLLING_FILE` appender as a `RollingFileAppender` with:
   - `<file>` set to `logs/auto-time-marking.log`
   - `<rollingPolicy>` using `SizeAndTimeBasedRollingPolicy`
   - `<fileNamePattern>` set to `logs/archived/auto-time-marking-%d{yyyy-MM-dd}.%i.log`
   - `<maxFileSize>` set to `10MB`
   - `<maxHistory>` set to `5`
   - `<totalSizeCap>` set to `50MB`
   - `<encoder>` using the shared pattern property
6. Define the `<root>` logger at `INFO` level with both `CONSOLE` and `ROLLING_FILE` appender references.
7. Validate the XML is well-formed.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/resources/logback-spring.xml` | Create | Confirmed | Task file, Tech Spec § 3 | Primary deliverable |

## Implementation Steps

1. **Create `logback-spring.xml`**: Create the file at `src/main/resources/logback-spring.xml` with XML declaration `<?xml version="1.0" encoding="UTF-8"?>`.

2. **Add configuration root**: Add `<configuration>` root element with `scan="true"` and `scanPeriod="30 seconds"` for development convenience (allows hot-reload of logging config without restart).

3. **Add MaskingConverter placeholder**: Add a commented-out `<conversionRule>` element:
   ```xml
   <!-- TODO: Activate after MaskingConverter is implemented in task 002 -->
   <!-- <conversionRule conversionWord="maskedMsg"
        converterClass="com.lucasbdourado.autotimemarking.shared.infrastructure.logging.MaskingConverter" /> -->
   ```

4. **Define log pattern property**: Add `<property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n" />`.

5. **Define CONSOLE appender**:
   ```xml
   <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
       <encoder>
           <pattern>${LOG_PATTERN}</pattern>
       </encoder>
   </appender>
   ```

6. **Define ROLLING_FILE appender**:
   ```xml
   <appender name="ROLLING_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
       <file>logs/auto-time-marking.log</file>
       <encoder>
           <pattern>${LOG_PATTERN}</pattern>
       </encoder>
       <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
           <fileNamePattern>logs/archived/auto-time-marking-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
           <maxFileSize>10MB</maxFileSize>
           <maxHistory>5</maxHistory>
           <totalSizeCap>50MB</totalSizeCap>
       </rollingPolicy>
   </appender>
   ```

7. **Define root logger**:
   ```xml
   <root level="INFO">
       <appender-ref ref="CONSOLE" />
       <appender-ref ref="ROLLING_FILE" />
   </root>
   ```

8. **Validate XML**: Verify the file is well-formed XML and Spring Boot starts without Logback configuration errors.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| `src/main/resources/logback-spring.xml` exists and is well-formed XML | Implementation step 1 creates the file | `mvn clean compile` succeeds; file exists at expected path |
| A `CONSOLE` appender is defined and writes to stdout | Implementation step 5 | Appender element with `class="ch.qos.logback.core.ConsoleAppender"` present in XML |
| A `ROLLING_FILE` appender is defined and writes to `logs/auto-time-marking.log` | Implementation step 6 | Appender element with `<file>logs/auto-time-marking.log</file>` present in XML |
| Log format pattern matches `[Timestamp] [Thread] [Level] [Logger] - Message` | Implementation step 4 — pattern `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n` | Pattern property defined and used in both appender encoders |
| Rolling policy uses `SizeAndTimeBasedRollingPolicy` with 10MB max file size, 5 max history, and 50MB total cap | Implementation step 6 | Rolling policy element with correct class and values in XML |
| Rolled files follow pattern `logs/archived/auto-time-marking-%d{yyyy-MM-dd}.%i.log` | Implementation step 6 | `<fileNamePattern>` element value in XML |
| Root logger is set to `INFO` level with both appenders attached | Implementation step 7 | `<root level="INFO">` with two `<appender-ref>` elements |
| Spring Boot application starts without Logback configuration errors | Implementation step 8 | Application starts; no Logback errors in console output |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `mvn clean compile` | Build | Verify the XML is on the classpath and the project compiles | Quick feedback on XML well-formedness within Maven lifecycle |
| Spring Boot application start | Manual | Verify Logback initializes without errors, console output appears with correct format, and `logs/auto-time-marking.log` file is created | Run the main class or `mvn spring-boot:run` and inspect console and file output |
| Inspect `logs/auto-time-marking.log` | Manual | Verify the log file is created and entries match the expected format pattern | Check timestamp, thread, level, logger, and message alignment |

Note: Formal unit and integration tests for the logging subsystem are covered in task 003.

## Dependencies

- No task dependencies. TSK-AL-001 is the first task in the audit-logging feature.
- Runtime dependency: `spring-boot-starter` in `pom.xml` provides Logback transitively. This dependency is already present.
- No external system dependencies.

## Risks and Edge Cases

- **Invalid XML syntax**: An XML syntax error would prevent Spring Boot from starting. Mitigation: validate XML before committing.
- **Log directory permissions**: Logback needs write access to `logs/` and `logs/archived/`. On most development environments this is not an issue. On production/server environments, file permissions should be verified. This is a general deployment concern, not specific to this task.
- **Commented-out conversionRule**: The placeholder comment must not break XML parsing. Using standard XML comments (`<!-- -->`) is safe.
- **`scan="true"` in production**: Enabling config scanning is convenient for development but has minimal overhead for a single-user MVP app running every 30 minutes. If production performance becomes a concern, `scan` can be set to `false` later.

## Rollback or Recovery Notes

- The deliverable is a single new file (`logback-spring.xml`). Rollback is straightforward: delete the file. Spring Boot will revert to its default console-only logging behavior.
- No existing files are modified by this task.

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

- Read the Tech Spec § 3 "Logback Configuration" before writing the XML to ensure exact parameter values.
- The log pattern uses `%logger{36}` (abbreviated logger name up to 36 characters) which maps to the `[Logger]` element in the human-readable format description.
- The `conversionRule` element must remain commented out. Task 002 will uncomment it after the `MaskingConverter` class exists. If uncommented prematurely, Spring Boot will fail to start with a `ClassNotFoundException`.
- Do not add `logging.file.name` or `logging.file.path` properties to `application.properties` — the `logback-spring.xml` takes full control of file output configuration.
- Do not create the `logs/` or `logs/archived/` directories manually. Logback creates them automatically on first write.
- After creating the file, run `mvn clean compile` and start the application to validate.
