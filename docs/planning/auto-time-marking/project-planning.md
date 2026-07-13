# Project Planning

## Status

Status: Confirmed

Planning readiness: Ready

Last updated: 2026-07-13

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Project Discovery | docs/context/project-discover.md | Discovery | Confirmed | Complete context available |
| Full Product PRD | docs/product/auto-time-marking/full-product-prd.md | Full Product PRD | Confirmed | Approved 2026-07-13 |
| MVP PRD | docs/product/auto-time-marking/mvp-prd.md | MVP PRD | Confirmed | Approved 2026-07-13 |

## Planning Scope

The project planning scope defines the blueprint for the MVP of the Auto Time Marking system. The primary focus of this plan is to deliver a reliable, single-user background automation system.

In scope:
- Scheduler loop checking every 30 minutes between 05:00 and 22:00, Monday to Friday.
- Headless browser interaction with BMAquiosque (login, navigation, marking detection, and punch submission).
- Workday marking calculation (flexible entry, 6h lunch limit, 1-2h lunch duration, 8h45 work calculation, and time jitter).
- Core credential and configuration loading (file/env-based).
- File-based logging for auditing and validation.

Out of scope:
- Detailed codebase analysis.
- Final technology decisions.
- Technical specification.
- Granular task creation.
- Task implementation plans.
- Post-MVP feature files (e.g., Discord Bot, Multi-user support, Dashboard, Web API).

## Product Vision Summary

Auto Time Marking is designed to be a server-side background service that automates daily time-clock registrations (ponto) on the BMAquiosque platform. It aims to eliminate forgotten markings and ensure record consistency for employees with flexible working hours. In its full vision, users interact with the system via a Discord bot to perform registrations, manage credentials, customize settings, and receive real-time notifications about their markings and system status.

## MVP Planning Focus

The MVP is focused strictly on single-user automation without any interactive interfaces (no Discord bot, no web dashboard) or external notification channels. This allows validating the core technical feasibility of headless browser automation with BMAquiosque and proving the correctness of the marking calculation engine before designing user management and communication layers.

## MVP Scope Summary

The MVP will execute all 4 workday markings autonomously for a single user using credentials and configuration files provided on startup.

Included in MVP:
- Configurable maximum entry time, lunch boundaries, and jitter.
- Headless browser automation (login, status parsing, marking submission).
- Calculation engine reflecting actual markings and lunch durations.
- 30-minute interval scheduler operating within Mon-Fri, 05:00-22:00.
- Retry mechanism (3 attempts, 5-minute intervals).
- File-based logging of all system actions.

Deferred beyond MVP:
- Discord bot interface.
- Real-time notification system (Discord messages).
- Multi-user database and security management.
- Pause/resume commands.
- Holiday management calendar.
- API layer and Web Dashboard.

## MVP Features Summary

| Feature | Goal | Priority | Dependencies | Notes |
| --- | --- | --- | --- | --- |
| `single-user-configuration` | Load and validate credentials and user parameters at startup | Must | None | Required for system boot |
| `activity-scheduler` | Execute check cycles every 30 minutes during work hours | Must | `single-user-configuration` | Drives the automation loop |
| `bmaquiosque-automation` | Perform headless browser login, status checking, and punch registration | Must | `single-user-configuration` | Validates site communication |
| `marking-calculation` | Decide when to submit punches, adjusting for actual times and jitter | Must | `bmaquiosque-automation` | Enforces compliance rules |
| `audit-logging` | Log check results, decisions, actions, and errors to rotating files | Should | None | Crucial for system audit |

## MVP Feature Details

### MVP Feature: single-user-configuration

#### Goal
Provide a parser to read and validate BMAquiosque login credentials, maximum entry time, and time variation parameters from a local file or environment variables at startup.

#### User Value
Ensures the system can be configured without rebuilding the application, and starts only if parameters are logically sound.

#### Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-006 | User Management (Single-User Config) | MVP PRD |

#### Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically... | MVP PRD |

#### Expected Outcome
The system successfully loads credentials and parameters. If any validation rule (e.g. invalid time formats or negative jitter) fails, it reports a clear error and terminates immediately.

#### Dependencies
- None.

#### Risks
- Unencrypted storage of credentials in local files.

#### Feature Completion Criteria
- [ ] System parses configuration on boot.
- [ ] Configuration bounds (VR-001, VR-002, VR-003) are strictly validated.
- [ ] Startup failure is triggered and logged if configurations are invalid.

#### Readiness Notes for Tech Spec
- Needs spec on configuration schema, supported data types, and file path locations.

#### Inputs for Create Tasks
- Create tasks for writing config parser, properties validator, and startup bootstrap logic.

---

### MVP Feature: activity-scheduler

#### Goal
Build an internal background runner that executes a marking check cycle every 30 minutes, restricted to the hours of 05:00 to 22:00, Monday to Friday.

#### User Value
Automates the system checking process without requiring external Cron configurations.

#### Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-002 | Scheduling System | MVP PRD / Full Product PRD |

#### Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically... | MVP PRD |

#### Expected Outcome
The scheduler runs continuously. It initiates a marking check every 30 minutes during operating hours and remains idle outside the window.

#### Dependencies
- `single-user-configuration` (for reading startup context).

#### Risks
- Thread death or silent scheduling stalls on the server.

#### Feature Completion Criteria
- [ ] Scheduled worker executes target task on a 30-minute interval.
- [ ] Day and hour bounds (Mon-Fri, 05:00-22:00) are enforced.
- [ ] Exceptions thrown during checks are caught to prevent scheduler crash.

#### Readiness Notes for Tech Spec
- Design of the scheduler thread/executor service model and timezone boundary logic.

#### Inputs for Create Tasks
- Create tasks for background executor implementation, time/day checks, and scheduler exception handler.

---

### MVP Feature: bmaquiosque-automation

#### Goal
Integrate a headless web browser engine that logs into BMAquiosque, extracts the list of already-registered markings for the day, and registers new markings.

#### User Value
Ensures the application can interact with BMAquiosque on behalf of the user, retrieving state and submitting data.

#### Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-001 | Time-Clock Automation Engine | MVP PRD / Full Product PRD |

#### Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-001 | As an employee, I want my time-clock entries to be registered automatically... | MVP PRD |
| MVP-US-004 | As an employee, I want the system to retry failed markings... | MVP PRD |

#### Expected Outcome
The service logs into BMAquiosque, returns a structured list of markings registered on the current day, registers a punch when requested, and retries up to 3 times in 5-minute intervals if network or element issues occur.

#### Dependencies
- `single-user-configuration` (for loading target credentials).

#### Risks
- Selector breakage due to changes in the BMAquiosque UI.
- Browser session memory leaks.

#### Feature Completion Criteria
- [ ] Successful login simulation to BMAquiosque.
- [ ] Extraction of today's marking history (types and times).
- [ ] Submission of a marking registration.
- [ ] Implementation of a 3-strike retry wrapper with a 5-minute delay.

#### Readiness Notes for Tech Spec
- Details on login flow, URL routes, target page selectors, and retry state machine.

#### Inputs for Create Tasks
- Create tasks for headless browser setup, login flow implementation, marking parser, punch clicker, and retry handler.

---

### MVP Feature: marking-calculation

#### Goal
Implement the core decision tree that checks today's registered markings, calculates pending times (respecting max entry time, lunch durations, and 8h45 work limits), and applies natural time variation.

#### User Value
Ensures automatic markings conform to business rules and appear organic rather than bot-generated.

#### Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-003 | Marking Calculation Logic | MVP PRD / Full Product PRD |

#### Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-002 | As an employee, I want the system to use my actual entry time... | MVP PRD |
| MVP-US-003 | As an employee, I want the system to add time variation... | MVP PRD |

#### Expected Outcome
Correct timing decisions are made. Jitter is calculated. Lunch duration changes are parsed, and the final exit time is dynamically recalculated to ensure 8h45 of effective work.

#### Dependencies
- `bmaquiosque-automation` (for retrieving current day state).

#### Risks
- Timezone discrepancies between the server and the BMAquiosque server.
- Calculations breaking due to edge-case manual entries.

#### Feature Completion Criteria
- [ ] Entry calculation logic triggers punch if max entry time is surpassed.
- [ ] Lunch-out logic schedules marking at most 6h after entry.
- [ ] Lunch-return logic registers punch after minimum 1h lunch.
- [ ] Exit logic recalculates duration based on actual entry and lunch.
- [ ] Jitter offset is randomly generated within configured limits and applied.

#### Readiness Notes for Tech Spec
- Strict state-machine definition for punch transitions and timezone normalizer logic.

#### Inputs for Create Tasks
- Create tasks for calculation algorithms, timezone utilities, and unit test suites for all business rule permutations.

---

### MVP Feature: audit-logging

#### Goal
Create a logging subsystem that records detailed logs of all scheduler events, marking evaluations, skips, successes, retries, and errors to rotating files.

#### User Value
Allows the developer/owner to verify the automation behavior and diagnose bugs without a database or UI.

#### Related PRD Capabilities
| Capability ID | Capability | Source |
| --- | --- | --- |
| CAP-007 | Logging and Audit | MVP PRD / Full Product PRD |

#### Related User Stories
| User Story ID | User Story | Source |
| --- | --- | --- |
| MVP-US-005 | As an employee, I want to see logs of what the system did... | MVP PRD |

#### Expected Outcome
Logs are formatted and appended to files. Rolling file mechanisms prevent the server from running out of disk space.

#### Dependencies
- None.

#### Risks
- Verbose debug logging filling storage space.

#### Feature Completion Criteria
- [ ] Logging configuration initialized at system boot.
- [ ] Successful actions and skips logged with timestamp and details.
- [ ] Failed events logged with error traces.
- [ ] Log rotation/rolling configured.

#### Readiness Notes for Tech Spec
- Specifications on log rotation sizes, log patterns, and log directory location.

#### Inputs for Create Tasks
- Create tasks for logging system configuration, event logger calls integration, and file rolling setup.

---

## Suggested Delivery Phases

### Phase 1: Foundation and Configuration
Goal: Set up the project chassis, configuration parsing, logging, and scheduling runner to ensure the background service is stable.

Suggested feature order:
1. `single-user-configuration`
2. `audit-logging`
3. `activity-scheduler`

Dependencies:
- Configuration validation must block the scheduler boot.
- Logging must be initialized before any parser runs.

Exit criteria:
- The application boots, parses parameters, schedules check cycles, logs its events, and halts cleanly on errors.

### Phase 2: Marking Automation and Rules
Goal: Connect to BMAquiosque, parse daily markings, apply work hour calculation logic, and perform automated punches with retry handlers.

Suggested feature order:
4. `bmaquiosque-automation`
5. `marking-calculation`

Dependencies:
- BMAquiosque page parsing depends on browser session setup.
- Marking calculation depends on parsed list of existing punches.

Exit criteria:
- Complete day automation cycle executes correctly (logs in, checks, calculates, and registers pending markings with jitter and retries).

---

## MVP Risks and Dependencies

| Risk or Dependency | Type | Impact | Mitigation or Follow-Up | Blocks Next Step? |
| --- | --- | --- | --- | --- |
| Credentials Exposure in Config Files | Risk | Medium | Design local configuration parsing to accept system environment variables to avoid writing passwords to disks. | Yes, before Technology Definition |
| BMAquiosque Selector Stability | External dependency | High | Implement modular page object patterns in the automation engine and take page screenshots on failed runs to debug changes. | Yes, before Tech Spec |
| BMAquiosque CAPTCHA introduction | Risk | High | Confirm whether BMAquiosque uses security challenges. Currently assumed clear. If introduced, automated bypass will block. | No |

## Product Gaps Found During Planning

*None (Source PRDs are comprehensive and aligned).*

## Open Questions

| Question | Impact | Blocks Next Step? | Suggested Owner |
| --- | --- | --- | --- |
| What is the exact URL and login flow of BMAquiosque? | Critical for automated navigation script | Yes, before Tech Spec | User |
| What is the exact DOM structure for checking and registering markings? | Critical for browser automation HTML selectors | Yes, before Tech Spec | Tech / User |
| What happens when the service is stopped mid-day and restarted? | Affects state evaluation (needs to ensure no double-marking occurs) | Yes, before Tech Spec | Tech (Assumption: re-check markings on startup) |

## MVP Planning Readiness

Status: Ready

Reason: The MVP scope is well-defined. The 16 PRD features are organized into 5 functional features. Dependencies, risks, and phases are mapped out. Core information needed to start technology definition is identified.

## MVP Planning Readiness Checklist

- [x] MVP scope is clear.
- [x] MVP features are identified.
- [x] Feature dependencies are mapped.
- [x] Suggested delivery phases are defined.
- [x] Suggested feature order is defined.
- [x] Blocking open questions are resolved or clearly marked.
- [x] Inputs for Technology Definition are listed.
- [x] Inputs for Tech Spec are listed.
- [x] Inputs for Create Tasks are listed.

## Inputs for Technology Definition

List of decisions required during the next workflow:
- Target Java Version (e.g. Java 17 or 21).
- Backend framework choice (e.g. Spring Boot vs Light java framework vs plain Java Main).
- Headless browser automation library (Playwright for Java vs Selenium).
- Scheduling library (ScheduledExecutorService vs Quartz vs Spring Scheduler).
- Configuration format (.env vs YAML vs JSON).
- Logging framework configuration (Logback / SLF4J).

## Inputs for Tech Spec

List of technical areas to specify:
- BMAquiosque login flow sequence and element selector selectors.
- State-machine transitions for the marking calculations.
- Timezone management details.
- Log pattern formats and rotation configuration.

## Inputs for Create Tasks

List of features to break down:
- Create tasks for `single-user-configuration`.
- Create tasks for `activity-scheduler`.
- Create tasks for `bmaquiosque-automation`.
- Create tasks for `marking-calculation`.
- Create tasks for `audit-logging`.

## Post-MVP Evolution Roadmap

| Phase | Focus | Capabilities Added | Notes |
| --- | --- | --- | --- |
| V2 | Multi-User & Discord Bot | CAP-004 (Discord Bot), CAP-005 (Notifications), CAP-006 (Multi-User) | Moves automation to a shared server for the team |
| V3 | Advanced Settings | CAP-008 (Holidays), CAP-009 (Dashboard), CAP-010 (API) | Full feature parity and management interface |

## Next Recommended Steps

- Proceed to the **Technology Definition** phase.
