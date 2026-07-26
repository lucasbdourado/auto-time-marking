# Task: Implement RetryingTimeClockClient Decorator

## Status

Depends on Previous Task

## Task ID

TSK-BMA-006

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `RetryingTimeClockClient` decorator class to wrap the base time clock client, providing exception-driven retries (up to 3 attempts, 5 minutes sleep between attempts).

## Context

To make the automation robust against transient BMAquiosque website issues or network hiccups, we need a retry mechanism. Implementing it as a Decorator wraps the client transparently for other modules.

## Scope

- Create a new class [RetryingTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/retry/RetryingTimeClockClient.java) under package `com.lucasbdourado.autotimemarking.modules.automation.infrastructure.retry` implementing `TimeClockClient`.
- Decorate the class with `@Component` and `@Primary` to ensure it is the default autowired bean for `TimeClockClient`.
- Inject the delegate client (the Playwright implementation) using constructor injection.
- Implement the interface methods:
  - `retrieveDailyMarkings(String username, String password)`
  - `registerMarking(String username, String password)`
- Implement a loop with up to 3 attempts:
  - If a call throws an Exception, catch it.
  - Log a warning using SLF4J (e.g., `"Attempt {} of 3 failed. Retrying in 5 minutes..."`).
  - Sleep for 5 minutes (`300000` ms) using `Thread.sleep()`.
  - Re-attempt.
  - If the 3rd attempt throws, propagate the exception.

## Out of Scope

- Implementing the Playwright page scraping logic or configuration property binding.

## Depends On

- 004-implement-timeclock-client-interface.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `RetryingTimeClockClient` wraps the Playwright client bean and behaves as a Decorator.
- The client attempts execution up to 3 times on exception with a 5-minute interval.
- The project compiles successfully.

## Implementation Notes

- Handle `InterruptedException` by restoring the interrupted status: `Thread.currentThread().interrupt();`.
- Use `@Primary` on `RetryingTimeClockClient` and specify a qualifier on the constructor (e.g., `@Qualifier("playwrightTimeClockClient")`) or name the beans clearly to avoid autowiring conflicts.

## Validation Notes

- Run `mvn clean compile` to check that the class compiles and Spring DI setup is valid.

## Risks

- Thread blocking: During retry intervals, the scheduler execution thread will be blocked. Since the scheduler runs sequentially with a pool of 1, this is acceptable for the single-user MVP, but must be documented.

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
