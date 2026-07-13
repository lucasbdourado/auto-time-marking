# Feature: marking-calculation

## Status

Status: Draft

Last updated: 2026-07-13

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Full Product PRD | docs/product/auto-time-marking/full-product-prd.md | Full Product PRD | Confirmed | Approved 2026-07-13 |
| MVP PRD | docs/product/auto-time-marking/mvp-prd.md | MVP PRD | Confirmed | Approved 2026-07-13 |
| Project Planning | docs/planning/auto-time-marking/project-planning.md | Planning | Confirmed | Approved 2026-07-13 |

## Feature Goal
Formulate the time calculation logic to determine which workday punches (entry, lunch-out, lunch-return, exit) are pending, compute their target times based on rules, and apply configured random jitter.

## User Value
Ensures that all daily registrations total exactly 8h45 of work time, respect variable lunch durations, and look natural to prevent system detection.

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
The engine inputs a list of today's existing markings and output decisions: which marking type is next, what is its target time, and whether the current time matches that target time (including a randomized jitter offset).

## Scope
- Entry marking timing: Triggers if current time >= max entry time and no entry exists.
- Lunch-out timing: Scheduled at entry time + 6 hours.
- Lunch-return timing: Scheduled at lunch-out time + 1 hour (default) or respects actual return.
- Exit timing calculation: Calculates exit = entry + actual lunch duration + 8h45.
- Jitter addition: Injects random offset within the user's config range (e.g. ±5 min).
- Recalculation logic: Handles shifts in lunch return to recalculate exit.

## Out of Scope
- Flexible shifts spanning past midnight.
- Support for overtime accumulation rules.

## Dependencies
| Dependency | Type | Required For | Status | Notes |
| --- | --- | --- | --- | --- |
| Marking lists | Feature | Evaluating status | Confirmed | Supplied by `bmaquiosque-automation` |

## Risks
| Risk | Impact | Likelihood | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| Double marking due to incorrect calculation state | High | Low | Ensure the state logic checks the exact presence of markings in BMAquiosque before making decisions. | Open |
| Timezone conversion bugs | Medium | Low | Standardize all internal calculations on a single local zone (e.g., America/Sao_Paulo). | Open |

## Feature Completion Criteria
- [ ] Calculations cover all 4 punch stages correctly.
- [ ] Variable lunch calculation (up to 2h) correctly adjusts exit target.
- [ ] Jitter randomizer applies values within configured bounds.
- [ ] High unit-test coverage of calculation matrices.

## Readiness Notes for Tech Spec
- Map the state transitions and calculations with precise Gherkin/logic specifications.

## Inputs for Create Tasks
- Create task for workday calculation engine algorithm.
- Create task for time jitter application helper.
- Create task for timezone utility normalizer.
