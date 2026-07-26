# Change Spec: WPE/Quiosque Single Source of Truth Integration

## 1. Overview
This technical change specification establishes `wpe/quiosque` (BMA Quiosque / Ahgora WPE Quiosque) as the absolute **Single Source of Truth (SSOT)** for all operations in `auto-time-marking`. All automated punch actions, daily marking extractions for worked-hours calculation (8h45m daily target), and periodic time clock status checks ("de tempos em tempos") must be directly executed against and derived from the `wpe/quiosque` system.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Playwright Client | [PlaywrightTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/playwright/PlaywrightTimeClockClient.java) | Direct web browser automation client for `wpe/quiosque` |
| Retry Wrapper | [RetryingTimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/retry/RetryingTimeClockClient.java) | Transient network resilience wrapper for `wpe/quiosque` requests |
| Workflow Orchestrator | [MarkingWorkflowOrchestrator.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/workflow/service/MarkingWorkflowOrchestrator.java) | Cycle orchestrator fetching live state from `wpe/quiosque` |
| Calculation Engine | [MarkingCalculatorService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorService.java) | Evaluates state and recalculates work target strictly from retrieved markings |
| Activity Scheduler | [ActivityScheduler.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java) | Triggers periodic checks ("de tempos em tempos") within operational window |
| Mock Mirror | [BmaQuiosqueMockController.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/infrastructure/mockserver/BmaQuiosqueMockController.java) | Local 1:1 endpoint mirror matching `/wpe/quiosque/` contracts |

## 3. Confirmed Facts vs Assumptions
### Confirmed Facts
- `wpe/quiosque` URL is configured dynamically via setting `bmaquiosque.url` (e.g. `http://joinville.neomind.com.br:8070/wpe/quiosque/` in production or `http://localhost:8080/wpe/quiosque/` in mock environment).
- Punch registration actions (`registerMarking`) interact directly with `wpe/quiosque` login and punch forms.
- Worked-hours calculations depend strictly on recorded daily markings retrieved from `wpe/quiosque` (`retrieveDailyMarkings`).
- Periodic execution is controlled by `ActivityScheduler` (default 30-minute interval) filtered by `SchedulerTimezoneFilter` (06:00-22:00 Mon-Fri).
- The application does not rely on local databases for marking persistence; it is stateless relative to `wpe/quiosque`.

### Assumptions & Open Questions
- All operational environments guarantee `wpe/quiosque` availability during the operational window (06:00 - 22:00).
- DOM element selectors for markings table and punch button on `wpe/quiosque` remain consistent with `bmaquiosque.selectors` configuration.

## 4. Current vs Expected Behavior
### Current Behavior
- Code components interact with `wpe/quiosque`, but architectural authority of `wpe/quiosque` as the sole authority for marking retrieval, calculation inputs, and periodic verification was not explicitly formalized into a dedicated change spec.
- In-memory state transient calculations were completed per cycle without explicit audit guarantees confirming stateless synchronization with `wpe/quiosque`.

### Expected Behavior
- **Sole Source of Markings**: `MarkingWorkflowOrchestrator` always fetches the current day's live markings directly from `wpe/quiosque` at the start of each execution tick.
- **Sole Target of Punch Actions**: `TimeClockClient.registerMarking` posts automated punches exclusively to `wpe/quiosque`.
- **Periodic Verification**: `ActivityScheduler` performs automated check cycles every 30 minutes (configurable) against `wpe/quiosque` to evaluate if a punch is required.
- **Stateless Recalculation**: If prior punches were registered manually by the user directly on `wpe/quiosque`, the system detects them dynamically on the next tick and recalculates exit targets accordingly.

## 5. Scope & Out of Scope
### In Scope
- Formalizing `wpe/quiosque` as the Single Source of Truth across all application components.
- Verification that `retrieveDailyMarkings`, `evaluateDecision`, and `registerMarking` flow end-to-end against `wpe/quiosque`.
- Ensuring stateless operation where external manual changes on `wpe/quiosque` are seamlessly ingested.
- Updating tests and configuration checks to enforce `wpe/quiosque` compliance.

### Out of Scope
- Direct SQL database access or persistent local caching of markings.
- Supporting alternative time clock portals outside `wpe/quiosque`.

## 6. Functional Acceptance Criteria

### AC-001: Live WPE Marking Retrieval
**Given** the application executing a periodic tick  
**When** `MarkingWorkflowOrchestrator.executeMarkingCycle()` runs  
**Then** it must invoke `TimeClockClient.retrieveDailyMarkings()` targeting the `wpe/quiosque` endpoint before any calculation occurs.

### AC-002: Dynamic Recalculation from WPE State
**Given** markings retrieved from `wpe/quiosque`  
**When** manual or previous automated markings exist (e.g. ENTRY at 08:00, LUNCH_OUT at 12:00, LUNCH_RETURN at 13:15)  
**Then** `MarkingCalculatorService` calculates the exact target exit time (`18:00`) dynamically using the `wpe/quiosque` markings array.

### AC-003: WPE Punch Registration
**Given** `PunchDecision.shouldPunch()` evaluates to `true`  
**When** the orchestrator triggers marking execution  
**Then** `TimeClockClient.registerMarking()` submits the punch directly to `wpe/quiosque` and verifies response.

### AC-004: Periodic Verification Cycle
**Given** application running within operational window (06:00 - 22:00, Mon-Fri)  
**When** the configured scheduler interval elapses  
**Then** an evaluation cycle is triggered automatically, inspecting `wpe/quiosque` status without manual intervention.

## 7. Technical Design & Contracts

### Sequence Diagram
```
ActivityScheduler (Every 30m tick)
       │
       ▼
MarkingWorkflowOrchestrator
       │
       ├───> 1. retrieveDailyMarkings() ───> WPE / Quiosque Endpoint
       │                                             │
       │<─── Returns List<LocalTime> ────────────────┘
       │
       ├───> 2. evaluateDecision(WorkdayState, currentTime)
       │         └── Calculates targets (8h45m workday + actual lunch)
       │
       └───> 3. IF shouldPunch == true
                 └──> registerMarking() ────> WPE / Quiosque Endpoint
```

## 8. Sequential Implementation Checklist

- [x] **Task 1: Verify WPE/Quiosque Configuration & Endpoints**
  - **Goal**: Confirm `BmaquiosqueProperties` and `BmaQuiosqueMockController` map to `/wpe/quiosque/`.
  - **Acceptance**: Endpoint properties strictly resolve to `/wpe/quiosque/` path structure.
  - **Dependencies**: None.

- [x] **Task 2: Audit Workflow Orchestration for SSOT Compliance**
  - **Goal**: Verify `MarkingWorkflowOrchestrator` fetches live state directly from `TimeClockClient` on every cycle.
  - **Acceptance**: Orchestrator never uses cached marking lists across cycles.
  - **Dependencies**: Task 1.

- [x] **Task 3: Validate Periodic Execution & Dynamic State Recalculation**
  - **Goal**: Ensure `ActivityScheduler` ticks periodically and recalculates targets dynamically if markings change on `wpe/quiosque`.
  - **Acceptance**: Unit & integration tests confirm dynamic recalculation when `wpe/quiosque` returns updated marking lists.
  - **Dependencies**: Task 2.

- [x] **Task 4: Comprehensive Test Suite & Verification**
  - **Goal**: Run full Maven test suite (`mvn clean test`) to confirm zero regressions across all 87+ tests.
  - **Acceptance**: `BUILD SUCCESS` with all integration and unit tests passing cleanly.
  - **Dependencies**: Task 3.
