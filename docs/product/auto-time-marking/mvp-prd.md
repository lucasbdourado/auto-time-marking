# MVP PRD

## Status

Status: Confirmed

Last updated: 2026-07-13

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## MVP Summary

The MVP is a single-user automation service that validates the core technical proposition: automatically registering time-clock entries on BMAquiosque via browser automation. It runs as a background process on a server, checking for existing markings every 30 minutes and registering pending ones to complete an 8h45 workday. Configuration is done via environment variables or a configuration file. Logging is written to files. There is no Discord bot, no multi-user support, and no notifications in this phase — the goal is to prove that the automation engine works reliably with BMAquiosque before building the interaction layer.

## Source Context

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Full Product PRD | docs/product/auto-time-marking/full-product-prd.md | Full Product PRD | Confirmed | Approved 2026-07-13 |
| User conversation | Gemini conversation 2026-07-13 | User input | Confirmed | MVP option selection and priority definition |

## Selected MVP Option

| Option Name | Focus | Decision Source | Rationale |
| --- | --- | --- | --- |
| Automação Single-User (sem Discord bot) | Validate core browser automation and marking calculation for a single user | User decision | Prioritizes fast delivery and validation of the core automation before investing in Discord bot and multi-user infrastructure; allows proving the BMAquiosque interaction works before building the full product |

## MVP Goal

- Validate that automated browser interaction with BMAquiosque can reliably check existing markings and register new ones.
- Validate that the marking calculation logic correctly handles flexible entry times, variable lunch durations (1–2h), and the 8h45 effective workday requirement.
- Deliver a working single-user automation that can run unattended on a server, Monday to Friday.
- Establish a solid technical foundation for future multi-user and Discord bot evolution.

## Problem Solved by the MVP

The MVP addresses the core problem of forgetting to register time-clock entries. For a single user, the system ensures that all four daily markings (entry, lunch-out, lunch-return, exit) are registered automatically on BMAquiosque, completing exactly 8h45 of effective work time. It eliminates the need for the user to manually access BMAquiosque four times per day.

## Target Users

| User or Actor | MVP Need | Priority | Notes |
| --- | --- | --- | --- |
| Single employee (the developer/owner) | Automated time-clock registration on BMAquiosque without manual intervention | Primary | Single-user only in MVP; multi-user deferred |

## MVP Scope

- Scheduler running every 30 minutes between 05:00 and 22:00, Monday to Friday.
- Automated browser interaction with BMAquiosque: login, navigate, check existing markings, register pending markings.
- Complete marking calculation logic for all 4 daily markings.
- Configurable maximum entry time.
- Lunch-out trigger: at most 6 hours after entry.
- Lunch-return trigger: after minimum 1h lunch, respecting max 2h.
- Exit calculation: entry time + actual lunch duration + 8h45 effective work.
- Recalculation when actual lunch duration differs from 1h.
- Configurable time jitter (variation) for natural-looking markings.
- Retry mechanism: up to 3 attempts, 5 minutes apart, on marking failure.
- Configuration via environment variables or configuration file (single user).
- BMAquiosque credentials stored in configuration (encrypted or environment variable).
- File-based logging of all actions (markings, retries, errors, skips).
- Detection and respect of already-registered manual markings.

## Out of Scope for MVP

- Discord bot (registration, configuration, notifications, pause/resume).
- Multi-user support and user management.
- Discord or any other notification channel.
- Pause/resume automation feature.
- Self-service user registration.
- Holiday management.
- Web dashboard.
- RESTful API.
- Admin role.
- Consultable history via any interface.
- Credential validation at setup time.

## Included Capabilities

| Capability ID | Capability | Why Included | Notes |
| --- | --- | --- | --- |
| CAP-001 | Time-Clock Automation Engine | Core value proposition; must validate that BMAquiosque interaction works | Single-user scope |
| CAP-002 | Scheduling System | Required to trigger automation checks at the right intervals and within operating window | |
| CAP-003 | Marking Calculation Logic | Required to correctly determine when each marking should be registered | Complete logic including variable lunch |
| CAP-007 | Logging and Audit | Needed to verify system behavior and diagnose issues without a notification channel | File-based logging only |

## Deferred Capabilities

| Capability ID | Capability | Why Deferred | Target Later Phase |
| --- | --- | --- | --- |
| CAP-004 | Discord Bot Interface | Not needed to validate core automation; adds complexity | Phase 2 (multi-user + Discord) |
| CAP-005 | Notification System | Depends on Discord bot; not needed for single-user validation | Phase 2 |
| CAP-006 | User Management | Single-user config file is sufficient for MVP | Phase 2 |
| CAP-008 | Holiday Management | Nice-to-have; user can stop the service manually on holidays | Phase 3+ |
| CAP-009 | Web Dashboard | Future enhancement | Phase 3+ |
| CAP-010 | API Layer | Future enhancement | Phase 3+ |

## MVP Features

| ID | Capability ID | Feature | Priority | Status |
| --- | --- | --- | --- | --- |
| MVP-F-001 | CAP-001 | Automated browser interaction with BMAquiosque (login, navigate, check markings, register marking) | Must | Confirmed |
| MVP-F-002 | CAP-001 | Detection of already-registered markings for the current day | Must | Confirmed |
| MVP-F-003 | CAP-001 | Automatic registration of pending markings at calculated times | Must | Confirmed |
| MVP-F-004 | CAP-001 | Retry mechanism: up to 3 attempts with 5-minute intervals on failure | Must | Confirmed |
| MVP-F-005 | CAP-002 | Internal scheduler running every 30 minutes | Must | Confirmed |
| MVP-F-006 | CAP-002 | Operating window: 05:00 to 22:00 | Must | Confirmed |
| MVP-F-007 | CAP-002 | Operating days: Monday to Friday only | Must | Confirmed |
| MVP-F-008 | CAP-003 | Entry marking: register at configured maximum time if no manual entry exists | Must | Confirmed |
| MVP-F-009 | CAP-003 | Lunch-out marking: register at most 6 hours after entry | Must | Confirmed |
| MVP-F-010 | CAP-003 | Lunch-return marking: register after lunch duration (min 1h, max 2h) | Must | Confirmed |
| MVP-F-011 | CAP-003 | Exit marking: calculate based on entry + actual lunch duration + 8h45 effective work | Must | Confirmed |
| MVP-F-012 | CAP-003 | Recalculate exit time when actual lunch duration differs from minimum | Must | Confirmed |
| MVP-F-013 | CAP-003 | Configurable time jitter per user for natural-looking markings | Must | Confirmed |
| MVP-F-014 | CAP-007 | Log all marking actions (success, failure, retry, skip) to file | Should | Confirmed |
| MVP-F-015 | CAP-007 | Log all system events (scheduler runs, errors) to file | Should | Confirmed |
| MVP-F-016 | CAP-006 | Single-user configuration via environment variables or config file | Must | Confirmed |

## MVP User Stories

| ID | User Story | Capability ID | Priority | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically on BMAquiosque so that I don't forget and create inconsistencies. | CAP-001 | Must | Confirmed | Core value |
| MVP-US-002 | As an employee, I want the system to use my actual entry time (if I marked manually) as the base for calculating the remaining markings so that my total workday is accurate. | CAP-003 | Must | Confirmed | |
| MVP-US-003 | As an employee, I want the system to add time variation to markings so that they look natural. | CAP-003 | Must | Confirmed | |
| MVP-US-004 | As an employee, I want the system to retry failed markings so that transient errors don't cause missed entries. | CAP-001 | Must | Confirmed | |
| MVP-US-005 | As an employee, I want to see logs of what the system did so that I can verify it's working correctly. | CAP-007 | Should | Confirmed | File-based in MVP |

## MVP Use Cases

| ID | Use Case | Actor | Goal | Related Capability | Notes |
| --- | --- | --- | --- | --- | --- |
| MVP-UC-001 | Configure automation | Employee | Set BMAquiosque credentials, max entry time, lunch preferences, and jitter via config file | CAP-003, CAP-007 | One-time setup |
| MVP-UC-002 | Automatic daily marking cycle | System | Check and complete all 4 daily markings for the configured user | CAP-001, CAP-002, CAP-003 | Runs autonomously |
| MVP-UC-003 | Review system logs | Employee | Check log files to verify markings were registered correctly | CAP-007 | Manual file inspection |

## MVP Expected Behaviors

| ID | Trigger or Condition | Expected Behavior | Related Capability | Status |
| --- | --- | --- | --- | --- |
| MVP-EB-001 | Scheduler fires (every 30 min, 05:00–22:00, Mon–Fri) | System checks the user's current markings on BMAquiosque | CAP-002, CAP-001 | Confirmed |
| MVP-EB-002 | No entry marking exists and current time ≥ configured max entry time | System registers entry marking with jitter | CAP-001, CAP-003 | Confirmed |
| MVP-EB-003 | No entry marking exists and current time < configured max entry time | System waits; no action | CAP-001, CAP-003 | Confirmed |
| MVP-EB-004 | Entry exists, no lunch-out, and ≥6h since entry | System registers lunch-out marking with jitter | CAP-001, CAP-003 | Confirmed |
| MVP-EB-005 | Lunch-out exists, no lunch-return, and ≥1h since lunch-out | System registers lunch-return marking with jitter | CAP-001, CAP-003 | Confirmed |
| MVP-EB-006 | Lunch-return exists, no exit, and calculated exit time reached | System registers exit marking with jitter | CAP-001, CAP-003 | Confirmed |
| MVP-EB-007 | Actual lunch duration > 1h | Exit time recalculated: entry + 8h45 + actual lunch duration | CAP-003 | Confirmed |
| MVP-EB-008 | Marking attempt fails | System retries up to 3 times, 5 minutes apart | CAP-001 | Confirmed |
| MVP-EB-009 | Marking fails after 3 retries | System logs the failure and stops retrying for that marking | CAP-001, CAP-007 | Confirmed |
| MVP-EB-010 | All 4 markings already exist | System skips; no action for the rest of the day | CAP-001 | Confirmed |
| MVP-EB-011 | Outside operating window or weekend | System does not run | CAP-002 | Confirmed |

## Main MVP Flow

### Flow 1: Automatic Daily Marking Cycle

1. Scheduler triggers at 30-minute interval (between 05:00 and 22:00, Monday to Friday).
2. System opens automated browser session and logs into BMAquiosque with configured credentials.
3. System checks which markings exist for the current day.
4. System determines which markings are pending.
5. For each pending marking whose trigger condition is met:
   a. System calculates the marking time with configured jitter.
   b. System registers the marking on BMAquiosque.
   c. System logs the successful action.
6. If no marking is needed at this time, system logs the check and exits.
7. System closes the browser session.
8. Scheduler waits for the next 30-minute cycle.

## Alternative Flows

| ID | Scenario | Flow or Behavior | Status |
| --- | --- | --- | --- |
| MVP-AF-001 | User manually registers some markings before the system runs | System detects existing markings and only registers the remaining ones, calculating times based on actual markings | Confirmed |
| MVP-AF-002 | User manually registers all 4 markings | System detects all markings are complete and skips for the day | Confirmed |
| MVP-AF-003 | User registers entry manually but forgets the rest | System uses the manual entry time as the base for calculating lunch and exit times | Confirmed |
| MVP-AF-004 | Actual lunch duration is 1h30 instead of 1h | System uses actual lunch duration and recalculates exit to maintain 8h45 effective work | Confirmed |

## Error and Empty States

| ID | Scenario | Expected User-Facing Behavior | Status |
| --- | --- | --- | --- |
| MVP-ES-001 | BMAquiosque is unreachable or times out | System retries up to 3 times (5 min apart), then logs the failure | Confirmed |
| MVP-ES-002 | BMAquiosque login fails (wrong credentials) | System logs the error; does not retry with same credentials | Confirmed |
| MVP-ES-003 | BMAquiosque changes its UI/structure | System fails to interact, logs error with details about unexpected page structure | Confirmed |
| MVP-ES-004 | No markings exist and max entry time has not been reached | System waits; logs the check as "no action needed" | Confirmed |
| MVP-ES-005 | Scheduler fails to start or crashes | System logs the error to stderr/file | Confirmed |

## Business Rules

| ID | Rule | Source | Status |
| --- | --- | --- | --- |
| MVP-BR-001 | A complete workday consists of exactly 8 hours and 45 minutes of effective work time | User | Confirmed |
| MVP-BR-002 | Each workday requires exactly 4 markings: entry, lunch-out, lunch-return, exit | User | Confirmed |
| MVP-BR-003 | Lunch break minimum duration is 1 hour | User | Confirmed |
| MVP-BR-004 | Lunch break maximum duration is 2 hours | User | Confirmed |
| MVP-BR-005 | Lunch-out must happen at most 6 hours after entry | User | Confirmed |
| MVP-BR-006 | Exit time = entry time + effective work time (8h45) + actual lunch duration | User | Confirmed |
| MVP-BR-007 | System operates only Monday to Friday | User | Confirmed |
| MVP-BR-008 | System operates only between 05:00 and 22:00 | User | Confirmed |
| MVP-BR-009 | If no manual entry exists by the configured max entry time, system registers entry automatically | User | Confirmed |
| MVP-BR-010 | On failure, system retries up to 3 times with 5-minute intervals | User | Confirmed |
| MVP-BR-011 | Each marking must include configurable time jitter to appear natural | User | Confirmed |

## Validation Rules

| ID | Input or Condition | Rule | Error Behavior | Status |
| --- | --- | --- | --- | --- |
| MVP-VR-001 | Max entry time in config | Must be between 05:00 and 22:00 | System fails to start with clear error message | Confirmed |
| MVP-VR-002 | Time jitter in config | Must be a non-negative integer (minutes) | System fails to start with clear error message | Confirmed |
| MVP-VR-003 | BMAquiosque credentials in config | Must be non-empty login and password | System fails to start with clear error message | Confirmed |
| MVP-VR-004 | Lunch duration | Must be between 1h and 2h | System enforces min/max bounds during calculation | Confirmed |
| MVP-VR-005 | Marking registration | Must not register a marking that already exists for that type on the same day | System skips already-existing markings | Confirmed |

## Permissions and Access Rules

| ID | Actor or Role | Permission or Restriction | Status |
| --- | --- | --- | --- |
| MVP-AR-001 | Configured user (single) | System operates on their behalf using stored credentials | Confirmed |
| MVP-AR-002 | System (automation engine) | Can access BMAquiosque using configured credentials | Confirmed |

## Acceptance Criteria

- [ ] System starts and validates configuration (credentials, max entry time, jitter).
- [ ] Scheduler runs every 30 minutes between 05:00 and 22:00, Monday to Friday.
- [ ] Scheduler does not run outside the operating window or on weekends.
- [ ] System logs into BMAquiosque via automated browser and checks existing markings.
- [ ] System correctly identifies which markings are already registered for the day.
- [ ] System registers entry marking at configured max entry time (+ jitter) when no manual entry exists.
- [ ] System registers lunch-out marking at most 6h after entry (+ jitter).
- [ ] System registers lunch-return marking after minimum 1h lunch (+ jitter).
- [ ] System calculates and registers exit marking to complete exactly 8h45 effective work (+ jitter).
- [ ] System recalculates exit time when actual lunch duration differs from 1h.
- [ ] System respects manual markings already registered and adjusts calculations.
- [ ] System does not register a marking that already exists.
- [ ] System retries failed markings up to 3 times, 5 minutes apart.
- [ ] System logs all actions (check, mark, retry, skip, error) to file.
- [ ] Time jitter produces variation within the configured range.
- [ ] All 4 markings for a complete workday can be registered automatically without manual intervention.

## Assumptions

- BMAquiosque's web interface does not use CAPTCHA, biometric, or geolocation validation.
- BMAquiosque does not limit markings per day or block automated access.
- BMAquiosque's web interface is accessible via headless browser automation.
- The server hosting the system has reliable internet connectivity.
- The user has a valid BMAquiosque account.
- Configuration is provided before the system starts (no runtime configuration changes in MVP).

## Open Questions

| Question | Impact | Owner or Next Step |
| --- | --- | --- |
| What is the exact URL and login flow of BMAquiosque? | Required for browser automation implementation | User to provide during implementation |
| What is the exact page structure for checking and registering markings? | Required for browser automation selectors | User to provide or agent to discover during implementation |
| Should the config file format be YAML, JSON, or .env? | Low impact; affects developer experience | Decide during technology definition |
| What happens when the service is stopped mid-day and restarted? | Edge case; system should pick up where it left off | Assumption: re-check markings on restart |

## MVP Completeness Checklist

- [x] MVP goal is defined.
- [x] MVP scope is clear.
- [x] Included capabilities are listed.
- [x] Deferred capabilities are listed.
- [x] MVP user stories are defined.
- [x] MVP expected behaviors are documented.
- [x] MVP acceptance criteria are defined.
- [x] MVP open questions are documented.

## Relationship to Full Product Vision

The MVP validates the **core technical risk** of the full product: whether BMAquiosque can be reliably automated via headless browser interaction. All four marking calculation rules (entry, lunch-out, lunch-return, exit with variable lunch) are fully implemented in the MVP, so the calculation engine is production-ready.

Once the MVP proves the automation works:
- **Phase 2** adds Discord bot integration (CAP-004), multi-user support (CAP-006), notifications (CAP-005), and pause/resume — transforming the tool into the full product for the group.
- **Phase 3+** adds holiday management (CAP-008), web dashboard (CAP-009), and API layer (CAP-010).

The MVP architecture should be designed to support multi-user evolution without requiring a full rewrite.

## Inputs for Next Harness Steps

- Project discovery: Project location is `c:\Users\lucas.dourado\IdeaProjects\auto-time-marking`. Greenfield project, no existing codebase. Target platform: BMAquiosque (external web system).
- Project planning: MVP focuses on single-user automation (Phase 1). Phase 2 adds Discord + multi-user. Phase 3+ adds dashboard + API. MVP has 16 features across 4 capabilities.
- Technology definition: Needs decisions on language/runtime, browser automation library (e.g., Playwright, Puppeteer, Selenium), scheduler approach, configuration format, logging library, and credential security approach. Must support headless browser on server.
- Technical specification: Browser automation interaction with BMAquiosque requires detailed analysis of the site's login flow, marking page structure, and HTML selectors. Retry and jitter logic need precise technical design.
- Design docs: Marking calculation engine design, scheduler integration, browser session management, configuration schema.
- Task breakdown: Suggested breakdown by capability: scheduler setup → browser automation → marking detection → marking calculation → marking registration → retry logic → jitter → logging → configuration → integration testing.
