# Feature Technical Specification: marking-calculation

## Status

Status: Confirmed

Last updated: 2026-07-26

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Feature Reference

`docs/features/marking-calculation/feature.md`

Target output path: `docs/features/marking-calculation/tech-spec.md`

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Feature | [feature.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/feature.md) | Feature | Confirmed | Primary feature source |
| Project Planning | [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/planning/auto-time-marking/project-planning.md) | Planning | Confirmed | MVP context, phases, dependencies |
| Technology Definition | [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md) | Technology definition | Confirmed | Confirmed stack (Java 21, Spring Boot) |

## Specification Scope

This specification details the technical design for the marking calculation engine and workflow orchestrator. It covers domain models for workday punch types and states, state evaluation algorithm for pending punches, calculation of required work time (8h45 effective work + actual lunch duration), configurable random time jitter generation, and the `MarkingWorkflow` orchestration implementation connecting status retrieval and punch registration.

## Feature Summary

The `marking-calculation` module forms the core business decision unit of the Auto Time Marking application. It inspects today's existing punch entries (retrieved from BMAquiosque), determines which of the 4 daily markings (Entry, Lunch-Out, Lunch-Return, Exit) is pending next, computes the exact target time according to labour compliance rules and configured limits, applies a randomized time jitter for natural execution, and triggers a punch submission via `TimeClockClient` when the target time is reached.

## Feature Goal

Implement the complete decision tree, target time calculator, time jitter component, and workflow orchestrator that processes today's punch state and autonomously decides when and which punch to execute on BMAquiosque.

## Product Completion Criteria

- [ ] Domain models for `MarkingType`, `MarkingRecord`, `WorkdayState`, and `PunchDecision` defined.
- [ ] Calculation engine supporting entry (max entry time limit), lunch-out (max 6h work), lunch-return (min 1h, max 2h duration), and exit (8h45 effective work + actual lunch duration) implemented.
- [ ] Recalculation logic for exit time when actual lunch duration differs from 1 hour implemented.
- [ ] Jitter randomizer (`TimeJitterService`) for adding offset within configured range (`±bmaquiosque.jitter-minutes`) implemented.
- [ ] `MarkingWorkflow` implementation (`MarkingWorkflowOrchestrator`) coordinating `TimeClockClient` and decision engine implemented.
- [ ] Unit and integration test suite covering all punch combination scenarios implemented.

## Technical Goals

- Keep domain calculation pure and framework-decoupled so calculation logic can be unit-tested without Spring context or web browsers.
- Utilize Java 21 `record`s for immutable domain objects (`MarkingRecord`, `WorkdayState`, `PunchDecision`).
- Use `java.time` API (`LocalTime`, `Duration`, `ZonedDateTime`) for precise time arithmetic.
- Implement `MarkingWorkflow` (from package `com.lucasbdourado.autotimemarking.modules.scheduler.domain`) as a Spring `@Service`.
- Provide comprehensive logging for each evaluation cycle (log decision reasons, calculated targets, applied jitter, and completed punches).

## Non-Goals

- Managing flexible shifts crossing midnight (MVP operates strictly within same-day 05:00-22:00 window).
- Calculating overtime or extra hour compensation rules.
- Directly invoking Playwright API (delegated to `TimeClockClient` interface from `bmaquiosque-automation`).

## Confirmed Technology Decisions

| Area | Decision | Source | Applies To | Notes |
| --- | --- | --- | --- | --- |
| Language & Runtime | Java 21 | `technology-definition.md` | Whole project | Java 21 `record`s, pattern matching |
| Framework | Spring Boot 3.4.x | `technology-definition.md` | Whole project | Service components, DI |
| Configuration | properties format | `technology-definition.md` | Properties loading | `BmaquiosqueProperties` |
| Testing | JUnit 5 + Mockito | `technology-definition.md` | Unit/Integration Testing | Comprehensive matrix testing |

## Pending Technology Decisions

| Area | Pending Decision | Impact on Feature | Required Next Step |
| --- | --- | --- | --- |
| None | None | None | None |

## Applicable Guidelines and References

| Reference | Path | Applies To | Usage |
| --- | --- | --- | --- |
| Java Guidelines | [.agents/docs/architecture/coding-guidelines/README.md](file:///.agents/docs/architecture/coding-guidelines/README.md) | Package structure & design | Domain/Infrastructure isolation rules |

## Proposed Technical Approach

The feature will be implemented in two packages:
1. `com.lucasbdourado.autotimemarking.modules.calculation` (Domain models and pure calculation logic)
2. `com.lucasbdourado.autotimemarking.modules.workflow` (Workflow orchestrator implementing `MarkingWorkflow`)

### 1. Domain Models

```java
package com.lucasbdourado.autotimemarking.modules.calculation.domain;

public enum MarkingType {
    ENTRY(1, "Entrada"),
    LUNCH_OUT(2, "Saída para Almoço"),
    LUNCH_RETURN(3, "Retorno do Almoço"),
    EXIT(4, "Saída");

    private final int sequence;
    private final String label;

    MarkingType(int sequence, String label) {
        this.sequence = sequence;
        this.label = label;
    }

    public int getSequence() { return sequence; }
    public String getLabel() { return label; }
}
```

```java
package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.time.LocalTime;

public record MarkingRecord(MarkingType type, LocalTime time) {}
```

```java
package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.util.List;
import java.util.Optional;

public record WorkdayState(List<MarkingRecord> existingMarkings) {
    public boolean hasMarking(MarkingType type) {
        return existingMarkings.stream().anyMatch(m -> m.type() == type);
    }

    public Optional<MarkingRecord> getMarking(MarkingType type) {
        return existingMarkings.stream().filter(m -> m.type() == type).findFirst();
    }
}
```

```java
package com.lucasbdourado.autotimemarking.modules.calculation.domain;

import java.time.LocalTime;

public record PunchDecision(
    boolean shouldPunch,
    MarkingType nextType,
    LocalTime calculatedTargetTime,
    LocalTime jitteredTargetTime,
    String reason
) {
    public static PunchDecision noPunch(String reason) {
        return new PunchDecision(false, null, null, null, reason);
    }

    public static PunchDecision execute(MarkingType type, LocalTime target, LocalTime jitteredTarget, String reason) {
        return new PunchDecision(true, type, target, jitteredTarget, reason);
    }
}
```

### 2. Core Calculation Engine (`MarkingCalculatorService`)

The engine applies rules sequentially:
1. **If 0 markings exist**:
   - Next type: `ENTRY`.
   - Target entry time: `maxEntryTime` (e.g., 09:00).
   - Apply jitter offset `j`: `jitteredTarget = maxEntryTime + j`.
   - Decision: `shouldPunch = currentTime >= jitteredTarget`.
2. **If 1 marking (`ENTRY`) exists**:
   - Next type: `LUNCH_OUT`.
   - Maximum allowed work before lunch: 6 hours.
   - Target lunch-out time: `actualEntryTime + 6 hours`.
   - Apply jitter offset `j`: `jitteredTarget = target + j`.
   - Decision: `shouldPunch = currentTime >= jitteredTarget`.
3. **If 2 markings (`ENTRY`, `LUNCH_OUT`) exist**:
   - Next type: `LUNCH_RETURN`.
   - Minimum lunch duration: 1 hour (60 minutes).
   - Target lunch-return time: `actualLunchOutTime + 1 hour`.
   - Apply jitter offset `j`: `jitteredTarget = target + j`.
   - Decision: `shouldPunch = currentTime >= jitteredTarget`.
4. **If 3 markings (`ENTRY`, `LUNCH_OUT`, `LUNCH_RETURN`) exist**:
   - Next type: `EXIT`.
   - Effective work required: 8 hours 45 minutes (525 minutes).
   - Actual lunch duration: `Duration.between(actualLunchOutTime, actualLunchReturnTime)`.
   - Target exit time: `actualEntryTime + actualLunchDuration + 525 minutes`.
   - Apply jitter offset `j`: `jitteredTarget = target + j`.
   - Decision: `shouldPunch = currentTime >= jitteredTarget`.
5. **If 4 markings exist**:
   - Decision: `noPunch("Workday complete. All 4 markings registered.")`.

### 3. Time Jitter Service (`TimeJitterService`)

- Random integer generator in range `[-maxJitterMinutes, +maxJitterMinutes]`.
- Injectable `Random` instance for deterministic unit testing.

### 4. Workflow Orchestrator (`MarkingWorkflowOrchestrator`)

- Implements `com.lucasbdourado.autotimemarking.modules.scheduler.domain.MarkingWorkflow`.
- `@Service` class autowiring `TimeClockClient`, `MarkingCalculatorService`, `TimeJitterService`, and `BmaquiosqueProperties`.
- Flow:
  1. Fetch `WorkdayState` from `timeClockClient.getTodayMarkings()`.
  2. Call `markingCalculatorService.evaluateDecision(...)`.
  3. Log evaluation result with details.
  4. If `shouldPunch` is true, call `timeClockClient.registerMarking(decision.nextType())`.
  5. Log success or catch failure.

## Architecture Notes

- **Package Structure**:
  - `com.lucasbdourado.autotimemarking.modules.calculation.domain`
  - `com.lucasbdourado.autotimemarking.modules.calculation.service`
  - `com.lucasbdourado.autotimemarking.modules.workflow.service`

## Modules and Responsibilities

| Component | Responsibility | Inputs | Outputs |
| --- | --- | --- | --- |
| `MarkingType` | Enum of 4 daily punch types | None | Sequence, label |
| `MarkingRecord` | Value object for a punch record | Type, time | Immutable fields |
| `WorkdayState` | Value object for today's punch state | List of records | Query helper methods |
| `PunchDecision` | Value object for calculation result | Decision flags, times, reason | Immutable fields |
| `TimeJitterService` | Generates randomized time offset | Max jitter minutes | Jittered LocalTime |
| `MarkingCalculatorService` | Core decision algorithm | WorkdayState, LocalTime, config | PunchDecision |
| `MarkingWorkflowOrchestrator` | Implements `MarkingWorkflow` contract | Triggered by scheduler | Executes full check & punch cycle |

## Integration Contracts

| Producer | Consumer | Contract | Notes |
| --- | --- | --- | --- |
| `ActivityScheduler` | `MarkingWorkflowOrchestrator` | `void executeMarkingCycle()` | Spring DI injection |
| `MarkingWorkflowOrchestrator` | `TimeClockClient` | `List<MarkingRecord> getTodayMarkings()`, `void registerMarking(MarkingType)` | Automation bridge |

## Data Model

`Not applicable` — No persistent database in MVP.

## API or Interface Design

```java
package com.lucasbdourado.autotimemarking.modules.calculation.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.*;
import com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config.BmaquiosqueProperties;

import java.time.LocalTime;

public interface MarkingCalculatorService {
    PunchDecision evaluate(WorkdayState state, LocalTime currentTime, BmaquiosqueProperties properties, int jitterMinutes);
}
```

## Security & Reliability Considerations

- All calculation inputs use immutable records to prevent state mutation.
- Null checks and fallback checks ensure edge cases (e.g. manual punches registered out of sequence) are safely logged without crashing.

## Testing Strategy

- **Unit Tests**:
  - `MarkingCalculatorTest`: Test matrix for 0, 1, 2, 3, 4 markings; test exact exit recalculation with 1h, 1h15, 1h30, 2h lunch durations; test max entry time bound; test 6h lunch limit.
  - `TimeJitterServiceTest`: Test generated values remain strictly within `[-max, +max]`.
  - `MarkingWorkflowOrchestratorTest`: Test orchestrator behavior when `shouldPunch` is true/false, and handling client exceptions using Mockito mocks.
