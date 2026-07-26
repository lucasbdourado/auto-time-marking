# Feature Spec: bmaquiosque-automation

## 1. Overview
The `bmaquiosque-automation` feature provides the automated browser bridge connecting the Auto Time Marking service to the external BMAquiosque platform. It uses Playwright for Java to spin up a headless Chromium instance, log into BMAquiosque, extract existing daily punch records, and submit new punches when instructed by the calculation engine.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Feature Overview | [feature.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/bmaquiosque-automation/feature.md) | High-level requirements and scope |
| Technical Spec | [tech-spec.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/bmaquiosque-automation/tech-spec.md) | Architecture, interfaces, and selector designs |
| Reference Doc | [playwright.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/playwright.md) | Playwright Java lifecycle reference |
| POM Config | [pom.xml](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/pom.xml) | Playwright dependency definition |

## 3. Confirmed Facts vs Assumptions
### Confirmed Facts
- Playwright 1.49.0 dependency is already included and verified in `pom.xml`.
- Application framework is Spring Boot 3.4.x running Java 21.
- Configuration format is `application.properties` mapped via `BmaquiosqueProperties`.

### Assumptions & Open Questions
- BMAquiosque does not introduce CAPTCHA or Multi-Factor Authentication.
- Playwright Chromium binaries will be cached locally on the runner host.

## 4. Current vs Expected Behavior
### Current Behavior
- `pom.xml` contains the Playwright dependency.
- Configuration properties `BmaquiosqueProperties` load basic login parameters without selector mappings or URLs.
- No `TimeClockClient` interface or Playwright browser automation class exists yet in the codebase.

### Expected Behavior
- `BmaquiosqueProperties` includes configurable target URL and CSS selectors for login inputs, submit button, marking container, and punch button.
- Domain interface `TimeClockClient` defines methods `retrieveDailyMarkings` and `registerMarking`.
- `PlaywrightTimeClockClient` manages headless Chromium browser lifecycle, handles authentication, scrapes today's punches, clicks punch submit, and captures viewport screenshots to `logs/screenshots/` on error.
- `RetryingTimeClockClient` decorator wraps `TimeClockClient` to retry operations up to 3 times with a 5-minute delay on transient failures.

## 5. Scope & Out of Scope
### In Scope
- Extending `BmaquiosqueProperties` with selector and URL mappings.
- Validating selector and URL fields in `BmaquiosquePropertiesValidator`.
- Domain port `TimeClockClient` interface.
- Playwright headless browser implementation (`PlaywrightTimeClockClient`).
- Error screenshot capture mechanism.
- Exception-driven 3-attempt retry decorator (`RetryingTimeClockClient`).
- Unit and mock test suite.

### Out of Scope
- CAPTCHA or MFA bypass mechanisms.
- Session cookie persistence across scheduler cycles.
- Direct calculation of punch timing (handled by `marking-calculation`).

## 6. Functional Acceptance Criteria
### AC-001: Configuration Extension and Validation
**Given** `application.properties` with `bmaquiosque.url` and selector properties
**When** the Spring context initializes
**Then** `BmaquiosqueProperties` binds the values and `BmaquiosquePropertiesValidator` confirms non-blank format without boot error.

### AC-002: Retrieving Daily Markings
**Given** valid BMAquiosque credentials
**When** `retrieveDailyMarkings` is called
**Then** Playwright opens Chromium, logs in, parses existing punches from the dashboard, returns them chronologically, and closes the browser context.

### AC-003: Registering a Punch
**Given** valid BMAquiosque credentials and a pending punch trigger
**When** `registerMarking` is called
**Then** Playwright logs in, clicks the punch button, verifies submission, and closes the browser context.

### AC-004: Failure Diagnostic Screenshots & Retries
**Given** a page load error or missing selector
**When** an automation exception occurs
**Then** a failure screenshot is saved to `logs/screenshots/` and `RetryingTimeClockClient` retries up to 3 times in 5-minute intervals before propagating the error.

## 7. Technical Design & Contracts
- **Package Structure**:
  - `com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient`
  - `com.lucasbdourado.autotimemarking.modules.automation.infrastructure.playwright.PlaywrightTimeClockClient`
  - `com.lucasbdourado.autotimemarking.modules.automation.infrastructure.retry.RetryingTimeClockClient`
- **Domain Port Interface**:
  ```java
  public interface TimeClockClient {
      List<LocalTime> retrieveDailyMarkings(String username, String password) throws Exception;
      void registerMarking(String username, String password) throws Exception;
  }
  ```

## 8. Validation References & Regression Risks
- **Validation**: `mvn clean compile test`
- **Regression Risks**:
  - Unreleased browser OS processes if Playwright is not closed in `try-with-resources`.
  - Selector breakage on BMAquiosque UI updates (mitigated by externalized properties and screenshot diagnostics).

## 9. Implementation Checklist
- [x] **1. Add Playwright Dependency**
  - Goal: Add `com.microsoft.playwright:playwright` to `pom.xml`.
  - Acceptance: `mvn clean compile` succeeds.
  - Depends on: None
- [x] **2. Extend BmaquiosqueProperties with Selectors**
  - Goal: Add `url` and `Selectors` inner properties class.
  - Acceptance: Properties bind cleanly from `application.properties`.
  - Depends on: 1
- [x] **3. Update Properties Validator and Tests**
  - Goal: Enforce validation rules for URL and selector strings in `BmaquiosquePropertiesValidator`.
  - Acceptance: Invalid URLs or blank selectors reject boot; unit tests pass.
  - Depends on: 2
- [x] **4. Implement TimeClockClient Interface**
  - Goal: Define `TimeClockClient` domain interface.
  - Acceptance: Package `com.lucasbdourado.autotimemarking.modules.automation.domain` compiles cleanly.
  - Depends on: 1
- [x] **5. Implement PlaywrightTimeClockClient**
  - Goal: Implement headless browser navigation, login, scraping, punching, and screenshot capture.
  - Acceptance: Playwright opens, interacts, captures screenshots on error, and closes cleanly.
  - Depends on: 3, 4
- [x] **6. Implement RetryingTimeClockClient Decorator**
  - Goal: Implement Decorator wrapping `TimeClockClient` to retry up to 3 times with a 5-minute delay.
  - Acceptance: Retries on transient exceptions; unit tests verify retry loop.
  - Depends on: 4
- [x] **7. Implement Automation Unit & Integration Tests**
  - Goal: Write unit tests with Mockito mocks for client decorator and properties validation.
  - Acceptance: `mvn clean test` passes with zero failures.
  - Depends on: 5, 6
- [x] **8. Verification Task**
  - Goal: Verify whole feature build and tests.
  - Acceptance: `mvn clean compile test` passes with `BUILD SUCCESS`.
  - Depends on: 1 through 7
