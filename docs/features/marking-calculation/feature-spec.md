# Feature Spec: marking-calculation

## 1. Overview
The `marking-calculation` feature implements the core business calculation engine and workflow orchestrator for Auto Time Marking. It processes today's existing punches retrieved from BMAquiosque, determines which of the 4 daily markings (Entry, Lunch-Out, Lunch-Return, Exit) is pending, computes target trigger times based on labour compliance rules (8h45 effective work, max entry time, 6h max work limit before lunch, variable 1h-2h lunch duration), applies configurable pseudo-random time jitter, and orchestrates punch registration.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Feature Overview | [feature.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/feature.md) | High-level requirements and scope |
| Technical Spec | [tech-spec.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tech-spec.md) | Architecture, models, and decision algorithm |
| Scheduler Workflow Port | [MarkingWorkflow.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/domain/MarkingWorkflow.java) | Domain interface to be implemented by orchestrator |
| Properties Config | [BmaquiosqueProperties.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java) | Configured timing boundaries and jitter settings |

## 3. Confirmed Facts vs Assumptions
### Confirmed Facts
- Effective work target is fixed at 8 hours and 45 minutes (525 minutes).
- Workday requires exactly 4 punch markings in sequence: `ENTRY`, `LUNCH_OUT`, `LUNCH_RETURN`, `EXIT`.
- Maximum work time before lunch is 6 hours.
- Lunch duration ranges between 1 hour (minimum) and 2 hours (maximum).
- `MarkingWorkflow` interface is already defined in `com.lucasbdourado.autotimemarking.modules.scheduler.domain`.

### Assumptions & Open Questions
- Timezone for all calculations is local to `bmaquiosque.timezone` (default `America/Sao_Paulo`).
- Jitter offset is randomly generated per check cycle within `[-bmaquiosque.jitter-minutes, +bmaquiosque.jitter-minutes]`.

## 4. Current vs Expected Behavior
### Current Behavior
- `MarkingWorkflow` interface exists, but no implementation class is registered in Spring context.
- `ActivityScheduler` logs a warning when `MarkingWorkflow` is triggered because no bean implements the interface.
- No calculation domain models (`MarkingType`, `WorkdayState`, `PunchDecision`) exist yet.

### Expected Behavior
- Domain package `com.lucasbdourado.autotimemarking.modules.calculation.domain` contains Java 21 immutables (`MarkingType`, `MarkingRecord`, `WorkdayState`, `PunchDecision`).
- `MarkingCalculatorService` evaluates `WorkdayState` and produces precise target times and `shouldPunch` decisions.
- `TimeJitterService` generates pseudo-random integer offset bounded by configuration settings.
- `MarkingWorkflowOrchestrator` implements `MarkingWorkflow`, autowires `TimeClockClient` and `MarkingCalculatorService`, evaluates decisions on every 30-minute scheduler tick, and submits pending punches.

## 5. Scope & Out of Scope
### In Scope
- Domain models (`MarkingType`, `MarkingRecord`, `WorkdayState`, `PunchDecision`).
- Decision algorithm covering all 4 workday punch stages.
- Recalculation logic for exit time when actual lunch duration differs from 1 hour.
- `TimeJitterService` for organic time offset generation.
- `MarkingWorkflowOrchestrator` implementing `MarkingWorkflow`.
- Unit test suite with Mockito mocks and deterministic assertions.

### Out of Scope
- Shifts crossing past midnight.
- Overtime calculation or accumulation rules.
- Direct Playwright browser automation (delegated to `TimeClockClient`).

## 6. Functional Acceptance Criteria
### AC-001: Entry Punch Decision
**Given** no markings exist for today and current time >= `maxEntryTime` + jitter
**When** calculation engine evaluates
**Then** decision returns `shouldPunch = true` and `nextType = ENTRY`.

### AC-002: Lunch-Out Decision
**Given** `ENTRY` marking exists at `T1` and current time >= `T1 + 6h` + jitter
**When** calculation engine evaluates
**Then** decision returns `shouldPunch = true` and `nextType = LUNCH_OUT`.

### AC-003: Lunch-Return Decision
**Given** `LUNCH_OUT` marking exists at `T2` and current time >= `T2 + 1h` + jitter
**When** calculation engine evaluates
**Then** decision returns `shouldPunch = true` and `nextType = LUNCH_RETURN`.

### AC-004: Recalculated Exit Decision
**Given** `ENTRY` at `08:00`, `LUNCH_OUT` at `12:00`, `LUNCH_RETURN` at `13:30` (1h30 lunch)
**When** calculation engine evaluates exit target
**Then** target exit time is calculated as `08:00 + 1h30 + 8h45` = `18:15` (+ jitter).

### AC-005: Completed Workday
**Given** all 4 markings exist
**When** calculation engine evaluates
**Then** decision returns `shouldPunch = false` and reason "Workday complete".

## 7. Technical Design & Contracts
- **Package Structure**:
  - `com.lucasbdourado.autotimemarking.modules.calculation.domain`
  - `com.lucasbdourado.autotimemarking.modules.calculation.service`
  - `com.lucasbdourado.autotimemarking.modules.workflow.service`
- **Orchestrator Contract**:
  ```java
  @Service
  @Primary
  public class MarkingWorkflowOrchestrator implements MarkingWorkflow {
      @Override
      public void executeMarkingCycle() throws Exception;
  }
  ```

## 8. Validation References & Regression Risks
- **Validation**: `mvn clean compile test`
- **Regression Risks**:
  - Null Pointer Exceptions if `TimeClockClient` returns null lists (mitigated by non-null validation).
  - Out of bounds jitter values (mitigated by `TimeJitterService` range checks).

## 9. Implementation Checklist
- [x] **1. Define Marking Domain Models**
  - Goal: Define `MarkingType`, `MarkingRecord`, `WorkdayState`, `PunchDecision`.
  - Acceptance: Package compiles cleanly.
  - Depends on: None
- [x] **2. Implement Workday Calculation Engine**
  - Goal: Implement `MarkingCalculatorService` with 8h45 work, 6h lunch limit, and variable lunch exit recalculation.
  - Acceptance: Engine evaluates all 4 stages accurately.
  - Depends on: 1
- [x] **3. Implement Time Jitter Service**
  - Goal: Implement `TimeJitterService` for bounded random time offsets.
  - Acceptance: Random offset is strictly within configured range.
  - Depends on: 1
- [x] **4. Implement Marking Workflow Orchestrator**
  - Goal: Implement `MarkingWorkflowOrchestrator` implementing `MarkingWorkflow`.
  - Acceptance: Orchestrator fetches status, evaluates decision, and calls `TimeClockClient`.
  - Depends on: 1, 2, 3
- [x] **5. Write Calculation Unit Tests**
  - Goal: Create unit tests for calculation engine, jitter service, and orchestrator.
  - Acceptance: `mvn clean test` passes with 100% success rate.
  - Depends on: 2, 3, 4
- [x] **6. Verification Task**
  - Goal: Run complete build and test suite.
  - Acceptance: `mvn clean compile test` passes with `BUILD SUCCESS`.
  - Depends on: 1 through 5
