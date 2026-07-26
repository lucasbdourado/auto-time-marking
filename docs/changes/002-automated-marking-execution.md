# Change Spec: Automated Time Marking Execution

## 1. Overview
This technical change specification defines the operational and runtime model for automated time clock registrations (*marcações de ponto*) on the BMAquiosque platform using the existing system architecture. The change formalizes the automated execution loop, runtime configuration, headless Playwright browser interaction, retry strategies, and verification procedures for single-user autonomous operation.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Application Main | [AutoTimeMarkingApplication.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/AutoTimeMarkingApplication.java) | Spring Boot application entry point |
| Activity Scheduler | [ActivityScheduler.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java) | Periodic background trigger (default 30 min) |
| Operational Window Filter | [SchedulerTimezoneFilter.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilter.java) | Restricts execution window (06:00 to 22:00, Mon-Fri, America/Sao_Paulo) |
| Marking Workflow Orchestrator | [MarkingWorkflowOrchestrator.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/workflow/service/MarkingWorkflowOrchestrator.java) | Orchestrates status fetching, punch decision evaluation, and punch registration |
| Marking Calculation Service | [MarkingCalculatorService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorService.java) | Enforces 8h45m workday, 6h pre-lunch limit, and exit recalculation based on actual lunch duration |
| Time Jitter Service | [TimeJitterService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/TimeJitterService.java) | Applies randomized natural time variations |
| Playwright Client | [PlaywrightTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/playwright/PlaywrightTimeClockClient.java) | Performs Playwright headless browser navigation, login, marking retrieval, and punch registration |
| Retrying Time Clock Client | [RetryingTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/retry/RetryingTimeClockClient.java) | Handles transient network errors with 3-attempt backoff |
| Configuration Properties | [application.properties](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/resources/application.properties) | Configuration keys for target URL, credentials, max entry time, jitter, and timezone |

## 3. Confirmed Facts vs Assumptions

### Confirmed Facts
- Target daily effective work time is 8 hours 45 minutes (525 minutes).
- Workday sequence consists of 4 distinct punches: `ENTRY`, `LUNCH_OUT`, `LUNCH_RETURN`, `EXIT`.
- Pre-lunch work duration is capped at a maximum of 6 hours.
- Exit time is dynamically recalculated based on actual lunch duration recorded.
- Automated scheduling triggers every 30 minutes within the 06:00-22:00 window, Monday through Friday (`America/Sao_Paulo`).
- All 87 unit and integration tests are passing cleanly (`BUILD SUCCESS`).

### Assumptions & Open Questions
- Production environment URL defaults to `http://joinville.neomind.com.br:8070/wpe/quiosque/` (overridable via `BMAQUIOSQUE_URL`).
- Production credentials are provided via `BMAQUIOSQUE_USERNAME` and `BMAQUIOSQUE_PASSWORD` environment variables or `credentials.json`.
- BMAquiosque platform does not currently enforce CAPTCHA or multi-factor authentication (MFA) challenges on login.

## 4. Current vs Expected Behavior

### Current Behavior
- Code base supports both local mock server execution (`http://localhost:8080/wpe/quiosque/`) and production target URL.
- Background execution requires bootstrapping the Spring Boot application (`AutoTimeMarkingApplication`).

### Expected Behavior
- Continuous, unattended background service execution that automatically evaluates marking status and submits punches on BMAquiosque without human intervention.
- Graceful error recovery: network glitches or temporary web portal unavailability trigger up to 3 retries (5-minute backoff) before logging an audit failure.
- Complete audit logging of each cycle (evaluated state, target time, jitter applied, decision reason, and punch outcome).

## 5. Scope & Out of Scope

### In Scope
- Specification of the automated execution workflow end-to-end.
- Runtime environment configuration and credential binding.
- Exception handling, retry policies, and logging audit trail.
- Verification procedure via integration test suite and application startup verification.

### Out of Scope
- Interactive Discord bot integration or notification webhooks (planned for V2).
- Multi-user credential management (planned for V2).
- Web dashboard or UI controls (planned for V3).

## 6. Functional Acceptance Criteria

### AC-001: Autonomous 30-Minute Cycle Execution
**Given** the application running in background  
**When** the scheduler tick fires during operational hours (Mon-Fri, 06:00 - 22:00 `America/Sao_Paulo`)  
**Then** `MarkingWorkflowOrchestrator` automatically queries BMAquiosque for current daily markings.

### AC-002: Accurate Punch Decision & Registration
**Given** the current workday state retrieved from BMAquiosque  
**When** `MarkingCalculatorService` evaluates that a pending punch target time (plus jitter) has been reached or surpassed  
**Then** `PlaywrightTimeClockClient` executes browser navigation, logs in, clicks the mark punch button, and verifies confirmation modal/result.

### AC-003: Operational Window Gating
**Given** current time is outside 06:00 - 22:00 or on weekends  
**When** `ActivityScheduler` tick fires  
**Then** `SchedulerTimezoneFilter` intercepts execution, logs an informational skip message, and halts processing for that cycle.

### AC-004: Transient Error Resilience
**Given** a transient network error or element load failure during BMAquiosque communication  
**When** `RetryingTimeClockClient` catches the exception  
**Then** it retries the operation up to 3 times with 5-minute delays before throwing a persistent failure exception.

### AC-005: Audit Trail & Diagnostics
**Given** any cycle execution (whether punch registered, skipped, or failed)  
**When** evaluation completes  
**Then** exact status details (existing markings count, target calculated time, jitter offset, decision reason) are written to the rolling log files.

## 7. Technical Architecture & Workflows

### Execution Flow Sequence
```
Spring Boot Startup (AutoTimeMarkingApplication)
   │
   ▼
ActivityScheduler (@Scheduled fixedDelay = 30 min)
   │
   ▼
SchedulerTimezoneFilter (Checks Mon-Fri, 06:00 - 22:00 America/Sao_Paulo)
   │
   ├─ Outside Window ──> [Log Skip & Sleep]
   │
   └─ Inside Window
         │
         ▼
MarkingWorkflowOrchestrator.executeMarkingCycle()
   │
   ├──> RetryingTimeClockClient.retrieveDailyMarkings()
   │       └─ PlaywrightTimeClockClient (Chromium Headless -> BMAquiosque URL)
   │
   ├──> MarkingCalculatorService.evaluateDecision(state, currentTime, properties)
   │       ├─ Evaluates Entry / Lunch Out / Lunch Return / Exit targets
   │       └─ Applies TimeJitterService offset
   │
   └─ Decision: shouldPunch == true?
         ├─ YES ─> RetryingTimeClockClient.registerMarking() ─> Playwright Punch Submission
         └─ NO  ─> Log reason (e.g. "Waiting for target time 18:05:00")
```

### Runtime Environment Configuration (`application.properties`)
```properties
bmaquiosque.username=${BMAQUIOSQUE_USERNAME:user.test}
bmaquiosque.password=${BMAQUIOSQUE_PASSWORD:pass.test}
bmaquiosque.max-entry-time=${BMAQUIOSQUE_MAX_ENTRY_TIME:09:00}
bmaquiosque.jitter-minutes=${BMAQUIOSQUE_JITTER_MINUTES:5}
bmaquiosque.timezone=${BMAQUIOSQUE_TIMEZONE:America/Sao_Paulo}
bmaquiosque.url=${BMAQUIOSQUE_URL:http://joinville.neomind.com.br:8070/wpe/quiosque/}
```

## 8. Validation References & Regression Risks

- **Build & Verification Command**: `mvn clean compile test`
- **Key Test Suites**:
  - [FullWorkflowIntegrationTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/workflow/FullWorkflowIntegrationTest.java)
  - [ActivitySchedulerTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/ActivitySchedulerTest.java)
  - [MarkingCalculatorServiceTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorServiceTest.java)
  - [PlaywrightTimeClockClientTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/playwright/PlaywrightTimeClockClientTest.java)
- **Regression Risks & Precautions**:
  - UI Selector changes on BMAquiosque: Mitigated by modular page element selectors and screenshot capture on failure.
  - Double marking risk: Mitigated by checking registered daily markings prior to triggering any punch submission.

## 9. Implementation & Verification Checklist

- [x] **1. Core Workflow Orchestration**
  - Goal: Implement `MarkingWorkflowOrchestrator` combining status retrieval, decision evaluation, and punch execution.
  - Acceptance: `FullWorkflowIntegrationTest` passes end-to-end.
  - Depends on: None

- [x] **2. Operational Window & Scheduler Filter**
  - Goal: Enforce 06:00 - 22:00 Mon-Fri operational boundary in `SchedulerTimezoneFilter`.
  - Acceptance: `SchedulerTimezoneFilterTest` verifies exact day/hour boundaries.
  - Depends on: Task 1

- [x] **3. Headless Automation Resilience**
  - Goal: Ensure Playwright client handles modals, front-end response rendering, and dynamic URL configuration.
  - Acceptance: `PlaywrightTimeClockClientTest` and `RetryingTimeClockClientTest` pass.
  - Depends on: Task 2

- [x] **4. End-to-End Suite & Automated Build Validation**
  - Goal: Execute complete project test suite to verify 0 regressions across all 87 tests.
  - Acceptance: `mvn clean compile test` returns `BUILD SUCCESS` with 87 tests passing.
  - Depends on: Task 3
