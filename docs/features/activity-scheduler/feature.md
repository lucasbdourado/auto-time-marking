# Feature: activity-scheduler

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
Establish an internal background scheduling loop that triggers a workday status check and marking execution cycle every 30 minutes, restricted to the operating hours of 05:00 to 22:00, Monday to Friday.

## User Value
Automates the periodic verification and punch submissions throughout the day, removing the need for manual cron utility setup.

## Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-002 | Scheduling System | MVP PRD / Full Product PRD |

## Related PRD Features
| Feature ID | Feature | Source | Priority |
| --- | --- | --- | --- |
| MVP-F-005 | Internal scheduler running every 30 minutes | MVP PRD | Must |
| MVP-F-006 | Operating window: 05:00 to 22:00 | MVP PRD | Must |
| MVP-F-007 | Operating days: Monday to Friday only | MVP PRD | Must |

## Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically... | MVP PRD |

## Expected Outcome
The service runs in the background. Every 30 minutes, it checks the current local time and day. If inside the operational window (Mon-Fri, 05:00-22:00), it runs the marking evaluation flow. Otherwise, it logs a skip or sleeps.

## Scope
- 30-minute interval scheduler thread.
- Monday to Friday day filter.
- 05:00 to 22:00 local time window filter.
- Safe task invocation (exceptions in task do not kill scheduler).

## Out of Scope
- Scheduling multiple users on different timezones (uses server timezone).
- Dynamic check intervals.

## Dependencies
| Dependency | Type | Required For | Status | Notes |
| --- | --- | --- | --- | --- |
| Scheduling library decision | Technology decision | Scheduler mechanism | Pending | Determined in Technology Definition |

## Risks
| Risk | Impact | Likelihood | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| Scheduler stops silently due to OutOfMemory or thread death | High | Low | Implement robust try-catch block inside scheduler loop; configure alert logging. | Open |

## Feature Completion Criteria
- [ ] Scheduler runs at 30-minute intervals.
- [ ] Active time limits (05:00-22:00) and days (Mon-Fri) are verified before triggering the automation flow.
- [ ] Scheduler thread recovery is verified.

## Readiness Notes for Tech Spec
- Design the Java executor or scheduler thread pool architecture.

## Inputs for Create Tasks
- Create task for background loop thread implementation.
- Create task for time/day window filter logic.
