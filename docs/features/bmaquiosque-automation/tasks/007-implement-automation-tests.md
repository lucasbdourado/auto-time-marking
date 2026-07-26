# Task: Implement Automation Tests

## Status

Depends on Previous Task

## Task ID

TSK-BMA-007

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Write unit tests for the retry decorator and integration/UI tests for the Playwright client using mock network routing to verify selectors and HTML parsing.

## Context

We must test both the retry logic under mock exceptions and the HTML parsing/browser behavior of the Playwright automation client under isolated, simulated network environments (without hitting the real BMAquiosque site).

## Scope

- Create a unit test class `RetryingTimeClockClientTest.java` under `com.lucasbdourado.autotimemarking.modules.automation.infrastructure.retry` to:
  - Mock the delegate `TimeClockClient`.
  - Verify that a successful call does not sleep or retry.
  - Verify that a transient failure followed by success is retried once.
  - Verify that three consecutive failures result in throwing the exception.
- Create a test class `PlaywrightTimeClockClientTest.java` under `com.lucasbdourado.autotimemarking.modules.automation.infrastructure.playwright` to:
  - Inject or mock `BmaquiosqueProperties` with local URLs.
  - Use Playwright's network routing features (`page.route()`) to intercept HTTP requests.
  - Return mock HTML pages containing the username/password form and a dashboard containing the markings container table.
  - Verify that `retrieveDailyMarkings` successfully authenticates, parses the daily marking times, and returns them sorted.
  - Verify that `registerMarking` navigates and attempts the punch button click.
  - Verify that a failed operation causes a screenshot file to be created in `logs/screenshots/`.

## Out of Scope

- Performing tests on the live/real BMAquiosque server.
- Testing calculating scheduling loops (covered under scheduler features).

## Depends On

- 005-implement-playwright-timeclock-client.md
- 006-implement-retrying-timeclock-client-decorator.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `RetryingTimeClockClientTest` verifies retry counts, warning logs, and correct propagation of exceptions.
- `PlaywrightTimeClockClientTest` intercepts traffic and validates successful login, parsing, button clicks, and screenshot generation under failure.
- `mvn test` completes with 100% success rate on the new test files.

## Implementation Notes

- Since the default retry sleep interval is 5 minutes, configure `RetryingTimeClockClient` or its test settings to use a mock/reduced delay (e.g. 0 or 1ms) during tests to avoid slowing down the build, or mock/stub `Thread.sleep` calls.
- Use Playwright's network interception to load static HTML strings.

## Validation Notes

- Run `mvn test` to verify all test suites compile and pass.

## Risks

- Slow test runs if Playwright has to boot/teardown Chromium multiple times (keep test cases focused and clean up browsers).

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
