# Task: Implement Logging and Masking

## Status

Depends on Previous Task

## Task ID

TSK-SUC-005

## Feature

`docs/features/single-user-configuration/feature.md`

## Source Documents

- `docs/features/single-user-configuration/feature.md`
- `docs/features/single-user-configuration/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement startup logging for loaded configuration parameters, ensuring the sensitive BMAquiosque password is masked in all console and log file outputs.

## Context

For monitoring and debugging, we need to log settings on success. However, credentials must be protected against leaks in stdout and log files.

## Scope

- In `ConfigurationVerificationHook` (or upon successful validation), print a success log message containing the loaded configuration details.
- Required log format: `Loaded BMAquiosque configuration. User: [username], Max Entry Time: [maxEntryTime], Jitter: [jitterMinutes] min, Timezone: [timezone].`
- Mask or exclude the password value (e.g., replace it with `[PROTECTED]` or `*****`) in all log and console outputs.

## Out of Scope

- Setting up log file rotation or Logback appenders (this belongs to the `audit-logging` feature).

## Depends On

004-implement-verification-hook.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- Startup logs show configuration details upon successful validation.
- The user's plain-text password value is never printed to console or logs.

## Implementation Notes

- Use SLF4J logger API.
- Do not call `.toString()` on `BmaquiosqueProperties` if it exposes the password field in plain text.

## Validation Notes

- Run `mvn clean compile` to check for compilation issues.

## Risks

- Leak of passwords in logs if toString() or custom loggers are not carefully checked.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
