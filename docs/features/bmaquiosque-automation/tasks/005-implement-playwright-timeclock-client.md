# Task: Implement PlaywrightTimeClockClient

## Status

Depends on Previous Task

## Task ID

TSK-BMA-005

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Implement the `PlaywrightTimeClockClient` class that uses Playwright to perform automated login, scrap today's marking times, and submit punches, saving failure screenshots if navigation fails.

## Context

This is the core browser automation engine. It must run headlessly, handle browser processes safely using try-with-resources, and log failures/successes.

## Scope

- Create a new class [PlaywrightTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/playwright/PlaywrightTimeClockClient.java) under package `com.lucasbdourado.autotimemarking.modules.automation.infrastructure.playwright` implementing `TimeClockClient`.
- Decorate the class with `@Component` (or `@Service`) to allow Spring dependency injection.
- Inject `BmaquiosqueProperties` bean.
- Manage Playwright lifecycle inside nested try-with-resources:
  ```java
  try (Playwright playwright = Playwright.create()) {
      try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
          try (BrowserContext context = browser.newContext()) {
              try (Page page = context.newPage()) {
                  // Execution logic
              }
          }
      }
  }
  ```
- Implement `retrieveDailyMarkings(String username, String password)`:
  - Navigate to BMAquiosque URL.
  - Fill username selector and password selector, click login.
  - Wait for the markings container.
  - Scrape the texts matching the markings container selector.
  - Map text elements to a sorted List of `LocalTime`.
- Implement `registerMarking(String username, String password)`:
  - Navigate to BMAquiosque URL.
  - Fill username and password selectors, click login.
  - Wait for the punch button selector and click it.
- Implement failure screenshot capturing:
  - If any exception is thrown, capture a page screenshot using `page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("logs/screenshots/failure-" + System.currentTimeMillis() + ".png")))`.
  - Re-throw the exception.

## Out of Scope

- Implementing the retry mechanism (handled by the decorator in Task 006).

## Depends On

- 001-add-playwright-dependency.md
- 002-extend-bmaquiosque-properties-with-selectors.md
- 004-implement-timeclock-client-interface.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `PlaywrightTimeClockClient` class implements `TimeClockClient` and contains the described try-with-resources structure.
- Screenshot is correctly saved under `logs/screenshots/` upon caught automation exceptions.
- The project compiles successfully.

## Implementation Notes

- Configure Playwright page navigation timeout to 15 seconds to prevent thread blocking (as defined in performance considerations).
- Save screenshot directory to local filesystem path `logs/screenshots/` and log the screenshot path on error.

## Validation Notes

- Run `mvn clean compile` to check that the class compiles and dependencies are satisfied.

## Risks

- Playwright native Chromium executable setup might trigger download delays or require server dependencies (e.g. system libraries on Linux).
- Selectors might throw timeout/element not found exceptions if BMAquiosque landing is slow (mitigated by retry in Task 006).

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
