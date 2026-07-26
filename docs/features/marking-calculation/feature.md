# Feature: marking-calculation

## Status

Status: Confirmed

Last updated: 2026-07-26

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Full Product PRD | [full-product-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/full-product-prd.md) | Full Product PRD | Confirmed | Approved 2026-07-13 |
| MVP PRD | [mvp-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/mvp-prd.md) | MVP PRD | Confirmed | Approved 2026-07-13 |
| Project Planning | [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/planning/auto-time-marking/project-planning.md) | Planning | Confirmed | Approved 2026-07-13 |
| Technology Definition | [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md) | Technology Definition | Confirmed | Approved 2026-07-13 |

## Feature Goal

Formulate the time calculation logic to determine which workday punches (entry, lunch-out, lunch-return, exit) are pending, compute their target times based on business rules (8h45 effective work, max entry time, 6h max work before lunch, 1h-2h lunch duration), and apply configured random time jitter.

## User Value

Ensures that all daily registrations total exactly 8h45 of work time, respect variable lunch durations, and look natural to prevent automated system detection.

## Related PRD Capabilities

| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-003 | Marking Calculation Logic | MVP PRD / Full Product PRD |

## Related PRD Features

| Feature ID | Feature | Source | Priority |
| --- | --- | --- | --- |
| MVP-F-008 | Entry marking at max entry time if no manual exists | MVP PRD | Must |
| MVP-F-009 | Lunch-out at most 6h after entry | MVP PRD | Must |
| MVP-F-010 | Lunch-return after lunch duration (min 1h, max 2h) | MVP PRD | Must |
| MVP-F-011 | Exit calculation for exactly 8h45 of work | MVP PRD | Must |
| MVP-F-012 | Recalculate exit when lunch duration differs from 1h | MVP PRD | Must |
| MVP-F-013 | Configurable time jitter per user | MVP PRD | Must |

## Related User Stories

| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-002 | As an employee, I want the system to use my actual entry time... | MVP PRD |
| MVP-US-003 | As an employee, I want the system to add time variation... | MVP PRD |

## Expected Outcome

The calculation engine processes today's existing markings retrieved from BMAquiosque and produces concrete punch decisions: which marking type is next (ENTRY, LUNCH_OUT, LUNCH_RETURN, EXIT, or NONE), what is its target trigger time, whether current time has reached or passed that target (with jitter), and when all 4 markings are completed.

## Scope

- **Entry marking timing**: Triggers if current time >= max entry time and no entry marking exists.
- **Lunch-out timing**: Scheduled at entry time + 6 hours.
- **Lunch-return timing**: Scheduled at lunch-out time + 1 hour (default) or respects actual return.
- **Exit timing calculation**: Calculates exit = entry + actual lunch duration + 8h45 work.
- **Jitter addition**: Injects random offset within the user's config range (e.g., ±5 min).
- **Recalculation logic**: Handles shifts in lunch return to dynamically adjust exit target time.
- **Workflow Orchestration**: Connects `TimeClockClient`, calculation logic, and jitter generation to implement `MarkingWorkflow`.

## Out of Scope

- Flexible shifts spanning past midnight.
- Overtime calculation or accumulation rules.
- Direct interaction with HTML/Playwright (handled by `bmaquiosque-automation`).

## Dependencies

| Dependency | Type | Required For | Status | Notes |
| --- | --- | --- | --- | --- |
| `single-user-configuration` | Feature | Configuration loading | Confirmed | Provides `BmaquiosqueProperties` |
| `activity-scheduler` | Feature | Workflow interface | Confirmed | Defines `MarkingWorkflow` |
| `bmaquiosque-automation` | Feature | Status parsing and punch submission | Confirmed | Provides `TimeClockClient` |

## Risks

| Risk | Impact | Likelihood | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| Double marking due to incorrect state evaluation | High | Low | Explicitly evaluate existing punches retrieved from BMAquiosque before deciding to punch. | Open |
| Timezone conversion bugs | Medium | Low | Standardize all calculations using `bmaquiosque.timezone` (`ZoneId`). | Open |

## Feature Completion Criteria

- [ ] Calculations cover all 4 punch stages correctly.
- [ ] Variable lunch calculation (1h to 2h) correctly adjusts exit target.
- [ ] Jitter randomizer applies values within configured bounds.
- [ ] Orchestration implements `MarkingWorkflow` and coordinates `TimeClockClient` calls.
- [ ] High unit-test coverage across all business rule permutations.

## Readiness Notes for Tech Spec

- Map state transitions, calculation matrices, DTO models, and `MarkingWorkflow` implementation spec.

## Inputs for Create Tasks

- Create task for domain models (`MarkingType`, `MarkingRecord`, `PunchDecision`).
- Create task for calculation engine (`MarkingCalculatorService`).
- Create task for jitter application helper (`TimeJitterService`).
- Create task for `MarkingWorkflow` orchestrator (`MarkingWorkflowOrchestrator`).
- Create task for unit and integration testing suites.
