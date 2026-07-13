# Task Implementation Plan: Implement Logging and Masking

## Status

Status: Ready for Implementation

Last updated: 2026-07-13

Plan file: `docs/features/single-user-configuration/task-plans/005-implement-logging-and-masking-plan.md`

## Task Reference

Task ID: `TSK-SUC-005`

Task file: `docs/features/single-user-configuration/tasks/005-implement-logging-and-masking.md`

Task status: `Depends on Previous Task` (Prerequisite is complete in codebase)

## Feature Reference

Feature name: `single-user-configuration`

Feature file: `docs/features/single-user-configuration/feature.md`

Feature Tech Spec: `docs/features/single-user-configuration/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/single-user-configuration/tasks/005-implement-logging-and-masking.md` | Goal, Scope, Acceptance Criteria | Confirmed | Primary source for task bounds |
| Feature file | `docs/features/single-user-configuration/feature.md` | Feature Goal, Scope | Confirmed | Functional context |
| Feature Tech Spec | `docs/features/single-user-configuration/tech-spec.md` | Proposed Technical Approach, State/Error Handling, Security/Permissions | Confirmed | Primary technical design source |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions, Guidelines | Confirmed | Stack constraints (Java 21, Spring Boot) |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Configuration, Infrastructure Layer | Confirmed | Modular package structure conventions |

## Planning Scope

This planning session is scoped only to implementing configuration loading success logs, implementing password masking, and ensuring `BmaquiosqueProperties` does not print sensitive data in logs or console. It does not cover writing unit or integration tests (deferred to Task 006) or log rotation setups (deferred to audit-logging feature).

## Task Summary

Modify `ConfigurationVerificationHook` to log successful configuration details with masked password, and add a safe `toString()` implementation to `BmaquiosqueProperties` to ensure the plain-text password is protected.

## Execution Eligibility

Status: Eligible

Reason: The prerequisite task `004-implement-verification-hook.md` has been successfully implemented in the codebase (the hook class `ConfigurationVerificationHook` exists and functions correctly).

## Feature Context

To provide operational visibility at startup, the system must log loaded configuration details upon successful validation. However, credentials (passwords) must be strictly protected against stdout/stderr leaks or log file persistence.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach | Full | Yes | Prints a success log message containing loaded config details when validation is successful. |
| Security and Permissions | Full | Yes | Masks sensitive configuration values in bootstrap logs. |
| State and Error Handling | Partial | Yes | Success boot logs details with masked password. |
| Observability and Logging | Full | Yes | Success log signal is generated correctly at startup. |

Coverage assessment:
- Justifying Tech Spec section: `Proposed Technical Approach` & `Security and Permissions`
- Tech Spec sections implemented by this task: `Security and Permissions` (Log masking) and `Observability and Logging` (Success log)
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 (LTS) | `technology-definition.md` | Target runtime compile target. |
| Spring Boot 3.4.x | `technology-definition.md` | Bootstrapping lifecycle and SLF4J log infrastructure configuration. |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Infrastructure Layer Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Package structure conventions | Classes must remain under the infrastructure configuration package. |

## Existing Decisions Reviewed

No existing feature, ADR, or architecture decision was relevant to this task besides the confirmed technology definition.

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationVerificationHook.java` | Class definition and hook method | Hook to update | Modify `afterPropertiesSet()` to print success log. |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` | Class definition and getters | Properties source | Need to verify properties getters are available and override `toString()`. |

## Confirmed Scope

- Modify `ConfigurationVerificationHook` to log at `INFO` level when validation passes.
- Expected success log format: `Loaded BMAquiosque configuration. User: [username], Max Entry Time: [maxEntryTime], Jitter: [jitterMinutes] min, Timezone: [timezone].`
- Add a custom `toString()` method to `BmaquiosqueProperties` class to override default Object representation and output fields with masked password: `password='[PROTECTED]'`.
- Ensure plain-text password values are never passed to standard console outputs or SLF4J logs.

## Out of Scope

- Setting up log file rotation or Logback appenders (belongs to `audit-logging` feature).
- Writing configuration validation unit/integration tests (belongs to TSK-SUC-006).

## Proposed Implementation Approach

1. Modify `BmaquiosqueProperties` to override the `toString()` method, mapping `password` to `'[PROTECTED]'`.
2. Modify `ConfigurationVerificationHook`'s `afterPropertiesSet()` method:
   - When the `errors` list returned by the validator is empty, execute a success log statement:
     ```java
     logger.info("Loaded BMAquiosque configuration. User: {}, Max Entry Time: {}, Jitter: {} min, Timezone: {}.",
             properties.getUsername(),
             properties.getMaxEntryTime(),
             properties.getJitterMinutes(),
             properties.getTimezone());
     ```
3. Run `mvn clean compile` to verify there are no compilation errors.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` | Modify | Confirmed | Task Scope, Tech Spec | Add safe `toString()` override |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationVerificationHook.java` | Modify | Confirmed | Task Scope, Tech Spec | Add success log statement |

## Implementation Steps

1. Open `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` and add the `toString()` method:
   ```java
   @Override
   public String toString() {
       return "BmaquiosqueProperties{" +
               "username='" + username + '\'' +
               ", password='[PROTECTED]'" +
               ", maxEntryTime='" + maxEntryTime + '\'' +
               ", jitterMinutes=" + jitterMinutes +
               ", timezone='" + timezone + '\'' +
               '}';
   }
   ```
2. Open `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/ConfigurationVerificationHook.java`.
3. Locate `afterPropertiesSet()` and modify the success check block:
   ```java
   if (errors.isEmpty()) {
       logger.info("Loaded BMAquiosque configuration. User: {}, Max Entry Time: {}, Jitter: {} min, Timezone: {}.",
               properties.getUsername(),
               properties.getMaxEntryTime(),
               properties.getJitterMinutes(),
               properties.getTimezone());
       return;
   }
   ```
4. Run `mvn clean compile` from the workspace root to check for compile errors.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Startup logs show configuration details upon successful validation | Log success message under `afterPropertiesSet()` if `errors.isEmpty()` | Log message printed on successful startup |
| The user's plain-text password value is never printed to console or logs | Password field is not logged in Hook; `toString()` overrides password with `[PROTECTED]` | Check log output and call `toString()` |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `mvn clean compile` | Build check | Verify code compiles cleanly | Check build status |

## Dependencies

- **Prerequisite**: Task 004 `004-implement-verification-hook.md` (completed).
- **Successor**: Task 006 `006-implement-configuration-tests.md` (will test validation and success logging behaviour).

## Risks and Edge Cases

- **Accidental Log Injection**: Other system components might directly log `BmaquiosqueProperties` fields. Overriding `toString()` mitigates this for object logging, but developers must remain vigilant not to call `getPassword()` inside other log lines.

## Rollback or Recovery Notes

- Revert local changes in git for `BmaquiosqueProperties.java` and `ConfigurationVerificationHook.java`.

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

- Ensure the success log string matches the required format exactly.
- Do not let raw password log references slip into the implementation.
