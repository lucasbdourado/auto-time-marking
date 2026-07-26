# Change Spec: End-to-End Workflow Integration & Calculation Engine

## 1. Overview
This technical change specification documents the end-to-end integration of the `auto-time-marking` automation workflow, bridging Playwright web automation, dynamic work time calculations (8h45m workday), scheduled execution within a defined operational window (06:00 to 22:00 on weekdays), transparent environment toggling between local mock server and production, and resilient credential loading.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Activity Scheduler | [ActivityScheduler.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java) | Periodic tick execution (default 30 min) |
| Timezone & Window Filter | [SchedulerTimezoneFilter.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/SchedulerTimezoneFilter.java) | Operational window gatekeeper (06:00-22:00 Mon-Fri) |
| Workflow Orchestrator | [MarkingWorkflowOrchestrator.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/workflow/service/MarkingWorkflowOrchestrator.java) | Co-ordinates marking collection, calculation & punch execution |
| Calculator Engine | [MarkingCalculatorService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorService.java) | Enforces 8h45m workday, 6h pre-lunch cap & exit recalculation |
| Playwright Client | [PlaywrightTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/playwright/PlaywrightTimeClockClient.java) | Headless Playwright Chromium automation with modal resilience |
| Retry Decorator | [RetryingTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/retry/RetryingTimeClockClient.java) | Resilience wrapper for transient network/communication errors |
| Mock Mirror Controller | [BmaQuiosqueMockController.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/mockserver/BmaQuiosqueMockController.java) | 1:1 local mirror mock server on port 8080 |
| Mock Credentials Loader | [MockCredentialsLoader.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/mockserver/MockCredentialsLoader.java) | Dynamic JSON credential loader (`credentials.json`) |
| Properties Configuration | [application.properties](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/resources/application.properties) | Central configuration keys & `BMAQUIOSQUE_URL` env variable |
| E2E Integration Suite | [FullWorkflowIntegrationTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/workflow/FullWorkflowIntegrationTest.java) | End-to-end integration test suite |

## 3. Confirmed Facts vs Assumptions
### Confirmed Facts
- Daily effective workday target is fixed at 8 hours 45 minutes (525 minutes).
- Workday sequence consists of 4 distinct punches: `ENTRY`, `LUNCH_OUT`, `LUNCH_RETURN`, `EXIT`.
- Maximum continuous work limit prior to lunch break is 6 hours.
- Exit time is dynamically recalculated based on actual recorded lunch duration.
- Target environments are toggled seamlessly via setting `bmaquiosque.url` or environment variable `BMAQUIOSQUE_URL`.
- Local mock server operates on `http://localhost:8080/wpe/quiosque/` and production operates on `http://joinville.neomind.com.br:8070/wpe/quiosque/`.
- Mock server front-end initial load maintains `#retorno` div in `display: none` until positive AJAX response.
- `MockCredentialsLoader` retrieves default user credentials from `src/main/resources/credentials.json`.

### Assumptions & Open Questions
- Default timezone is fixed to `America/Sao_Paulo`.
- Operational window start time must be aligned to strictly 06:00:00 (updating current 05:00:00 filter setting in `SchedulerTimezoneFilter.java`).

## 4. Current vs Expected Behavior
### Current Behavior
- `SchedulerTimezoneFilter` opens operational window at 05:00:00 instead of 06:00:00.
- Components are fully functional individually and verified with 87 unit/integration tests, but lack an explicit unified change specification baseline (`001-full-workflow-integration`).

### Expected Behavior
- `SchedulerTimezoneFilter` restricts execution window strictly to 06:00:00 - 22:00:00, Monday through Friday, `America/Sao_Paulo` timezone.
- Seamless environment switching via `BMAQUIOSQUE_URL` variable without code recompilation.
- Front-end `#retorno` div remains hidden on load and renders feedback only upon valid AJAX punch response.
- Dynamic credential loading supports transient retry behavior via `RetryingTimeClockClient`.

## 5. Scope & Out of Scope
### In Scope
- Adjusting `SchedulerTimezoneFilter` window start time from 05:00 to 06:00.
- Documenting complete end-to-end architecture and environment toggle rules.
- Validating full workflow integration through automated integration test suite (`FullWorkflowIntegrationTest.java`).
- Guaranteeing front-end `#retorno` element visibility constraints.

### Out of Scope
- Support for weekend punches or shifts extending past midnight.
- Overtime accumulation or manual punch adjustments outside the 4 standard daily events.

## 6. Functional Acceptance Criteria

### AC-001: Operational Window Enforcement
**Given** an `ActivityScheduler` trigger tick  
**When** current time is outside 06:00 - 22:00 or on Saturday/Sunday (`America/Sao_Paulo`)  
**Then** the scheduler skips execution without invoking `MarkingWorkflowOrchestrator`.

### AC-002: Transparent Environment Toggle
**Given** the application configured with environment variable `BMAQUIOSQUE_URL`  
**When** set to `http://localhost:8080/wpe/quiosque/` or `http://joinville.neomind.com.br:8070/wpe/quiosque/`  
**Then** `PlaywrightTimeClockClient` navigates and performs automation transparently without code changes.

### AC-003: Dynamic Punch Recalculation
**Given** recorded markings `ENTRY` at 08:00, `LUNCH_OUT` at 12:00, and `LUNCH_RETURN` at 13:30 (1h30m lunch)  
**When** `MarkingCalculatorService` evaluates the exit target time  
**Then** target exit time is calculated as 18:15 (`08:00` + `01:30` + `08:45`).

### AC-004: Front-end Non-False Marking Guarantee
**Given** the BMA Quiosque marking web page loaded in Playwright browser  
**When** initial page render completes  
**Then** `#retorno` alert element has style `display: none;` and populates message content only after success AJAX punch response.

### AC-005: Resilient Credential & Network Operations
**Given** dynamic user credentials loaded from `credentials.json`  
**When** a transient network error occurs during marking retrieval or submission  
**Then** `RetryingTimeClockClient` executes retries with backoff up to maximum configured retry count (3 attempts).

## 7. Technical Design & Contracts

### Component Architecture & Sequence
```
ActivityScheduler (Fixed delay 30m)
   │
   ▼
SchedulerTimezoneFilter (06:00 - 22:00 Mon-Fri, America/Sao_Paulo)
   │
   ▼
MarkingWorkflowOrchestrator
   │
   ├───> RetryingTimeClockClient (3 retries, 5m delay)
   │        │
   │        ▼
   │     PlaywrightTimeClockClient (Headless Chromium, BMAQUIOSQUE_URL)
   │
   └───> MarkingCalculatorService
            │
            ▼
         WorkdayState & PunchDecision (8h45m workday, 6h cap before lunch, exit recalculation)
```

### Configuration Keys (`application.properties`)
```properties
bmaquiosque.username=${BMAQUIOSQUE_USERNAME:user.test}
bmaquiosque.password=${BMAQUIOSQUE_PASSWORD:pass.test}
bmaquiosque.max-entry-time=${BMAQUIOSQUE_MAX_ENTRY_TIME:09:00}
bmaquiosque.jitter-minutes=${BMAQUIOSQUE_JITTER_MINUTES:0}
bmaquiosque.timezone=${BMAQUIOSQUE_TIMEZONE:America/Sao_Paulo}
bmaquiosque.url=${BMAQUIOSQUE_URL:http://localhost:8080/wpe/quiosque/}
```

### `SchedulerTimezoneFilter.java` Contract Update
```java
private static final LocalTime START_TIME = LocalTime.of(6, 0, 0);
private static final LocalTime END_TIME = LocalTime.of(22, 0, 0);
```

## 8. Validation References & Regression Risks
- **Validation Commands**: `mvn clean compile test`
- **Key Test Files**:
  - `FullWorkflowIntegrationTest.java`
  - `AuthenticationBehaviorTest.java`
  - `ActivitySchedulerTest.java`
- **Regression Risks**:
  - Window start time discrepancy breaking early morning test cases (mitigated by updating unit test assertions in `SchedulerTimezoneFilterTest`).

## 9. Implementation Checklist

- [x] **1. Align Operational Window Start Time**
  - Goal: Update `SchedulerTimezoneFilter.java` start time constant to 06:00:00.
  - Acceptance: `SchedulerTimezoneFilterTest` passes and validates 06:00 start boundary.
  - Depends on: None

- [x] **2. Verify Front-End Mock Server Behavior**
  - Goal: Confirm `BmaQuiosqueMockController.java` maintains `#retorno` element hidden on load and updates only post-AJAX punch success.
  - Acceptance: Playwright integration test confirms element visibility lifecycle.
  - Depends on: Task 1

- [x] **3. Validate End-to-End Workflow & Environment Switching**
  - Goal: Run `FullWorkflowIntegrationTest.java` ensuring full coverage of local mock toggle and 8h45m workday recalculation.
  - Acceptance: All tests in full integration test suite pass cleanly (`BUILD SUCCESS`).
  - Depends on: Task 2

- [x] **4. Final Regression Test Verification**
  - Goal: Run entire project unit and integration test suite.
  - Acceptance: `mvn clean compile test` passes with 0 failures and 0 errors.
  - Depends on: Task 3
