# Feature: bmaquiosque-automation

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
Implement automated web browser navigation to interact with the BMAquiosque platform—performing user login, retrieving the list of currently registered punches for today, and submitting new time punches.

## User Value
Performs the actual communication with BMAquiosque, acting as the interface between system calculations and the external timekeeping platform.

## Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-001 | Time-Clock Automation Engine | MVP PRD / Full Product PRD |

## Related PRD Features
| Feature ID | Feature | Source | Priority |
| --- | --- | --- | --- |
| MVP-F-001 | Automated browser interaction (login, check, register) | MVP PRD | Must |
| MVP-F-002 | Detection of already-registered markings for current day | MVP PRD | Must |
| MVP-F-003 | Automatic registration of pending markings | MVP PRD | Must |
| MVP-F-004 | Retry mechanism: 3 attempts with 5-minute intervals | MVP PRD | Must |

## Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically... | MVP PRD |
| MVP-US-004 | As an employee, I want the system to retry failed markings... | MVP PRD |

## Expected Outcome
The system opens a headless browser, logs in, parses the table of current daily markings, registers a punch if commanded by the calculation engine, and retries the process on network or loading errors up to 3 times.

## Scope
- Headless browser instantiation and management.
- BMAquiosque login form submission.
- Marking retrieval and parsing into structured objects.
- Marking click trigger simulation.
- Exception-driven retry wrapper (up to 3 attempts, 5-minute sleep between retries).

## Out of Scope
- Bypassing CAPTCHAs, geofencing, or multi-factor authentication.
- Maintaining persistent sessions between scheduler runs (performs fresh login every cycle).

## Dependencies
| Dependency | Type | Required For | Status | Notes |
| --- | --- | --- | --- | --- |
| Headless browser library | Technology decision | Executing browser steps | Pending | Decided in Technology Definition |
| BMAquiosque URLs and element selectors | External / Documentation | Navigating and clicking elements | Pending | User to provide / Tech to document in Tech Spec |

## Risks
| Risk | Impact | Likelihood | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| BMAquiosque website UI changes, breaking selectors | High | Medium | Use robust selectors (ID-based, text-based) and take page source/screenshot dumps on failure. | Open |
| Login credentials expire/incorrect | Medium | Low | Log clear credential errors and do not retry with known invalid logins. | Open |

## Feature Completion Criteria
- [ ] Automation session can successfully log in.
- [ ] Automation session parses daily marking status.
- [ ] Automation session registers a pending marking.
- [ ] Retry algorithm executes on failure.

## Readiness Notes for Tech Spec
- Target login and landing page URLs, selectors for inputs, button clicks, and marking history tables.

## Inputs for Create Tasks
- Create task for browser session boot.
- Create task for login simulation.
- Create task for parsing current markings.
- Create task for executing a punch register.
- Create task for the retry loop handler.
