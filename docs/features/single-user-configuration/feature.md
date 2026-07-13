# Feature: single-user-configuration

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
Load and validate the user's BMAquiosque login credentials, maximum entry time, and marking variation (jitter) parameters from external sources (configuration files or environment variables) on service startup.

## User Value
Allows the user to easily configure their schedule preferences and BMAquiosque credentials without modifying the application code, and guarantees the service fails-fast with an informative error if configuration is incorrect.

## Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-006 | User Management (Single-User Config) | MVP PRD |

## Related PRD Features
| Feature ID | Feature | Source | Priority |
| --- | --- | --- | --- |
| MVP-F-016 | Single-user configuration via env vars or config file | MVP PRD | Must |

## Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically... | MVP PRD |
| MVP-US-003 | As an employee, I want the system to add time variation... | MVP PRD |

## Expected Outcome
The system boots, reads the target configuration, validates the input formatting and constraints, and successfully exposes the settings to other modules. If settings are invalid or missing, it logs the validation error and exits.

## Scope
- Reading configurations from environment variables or a local configuration file.
- Strict validation of maximum entry time (must be between 05:00 and 22:00).
- Strict validation of time jitter (must be a non-negative integer).
- Check that BMAquiosque login and password values are non-empty.

## Out of Scope
- Dynamic runtime updates of configuration (changing settings requires service restart).
- Validating the correctness of BMAquiosque credentials at startup by attempting a dummy login (this is handled in `bmaquiosque-automation`).

## Dependencies
| Dependency | Type | Required For | Status | Notes |
| --- | --- | --- | --- | --- |
| Configuration security decision | Technology decision | Secure storage of password | Pending | Determined during Technology Definition |

## Risks
| Risk | Impact | Likelihood | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| Credentials leaked via source control | High | Medium | Add configuration file names to `.gitignore` and encourage environment variables. | Open |

## Feature Completion Criteria
- [ ] Implementation of configuration parser.
- [ ] Validation constraints (VR-001, VR-002, VR-003) implemented and covered by unit tests.
- [ ] App exits with status code > 0 and error logging when configuration is invalid.

## Readiness Notes for Tech Spec
- Determine config schema keys and path location in the filesystem.

## Inputs for Create Tasks
- Create task for properties configuration file parsing.
- Create task for validation checks (max entry time, jitter bounds).
