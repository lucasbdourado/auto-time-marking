# Full Product PRD

## Status

Status: Confirmed

Last updated: 2026-07-13

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Summary

Auto Time Marking is a backend automation service that automatically registers time-clock entries (ponto) on the BMAquiosque platform on behalf of its users. The system runs on a centralized server, checks for existing markings every 30 minutes, and completes the remaining markings needed to fulfill an 8-hour-45-minute workday. Users interact exclusively through a Discord bot for registration, configuration, and notifications. Each user has individual settings including maximum time limits for each marking, configurable time variation for natural-looking entries, and the ability to pause automation temporarily.

## Source Context

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| User conversation with Lucas Dourado | Gemini conversation 2026-07-13 | User input | Confirmed | Initial product idea and all behavior definitions |

## Problem Statement

Users frequently forget to register their time-clock entries on the BMAquiosque platform. This causes inconsistencies in their work hour records, which can lead to payroll issues, compliance problems, and manual correction efforts. The problem affects a small group of colleagues who share the same flexible-hours work arrangement. Without an automated solution, each user must remember to manually access the BMAquiosque site four times per day (entry, lunch out, lunch return, exit) and any missed marking requires manual correction or causes record discrepancies.

## Goals

- Eliminate forgotten time-clock markings by automating the registration process.
- Ensure every workday has all four required markings that correctly total 8h45 of effective work time.
- Provide timely notifications via Discord when markings are made or when errors occur.
- Allow individual user configuration for maximum marking times and time variation.
- Support flexible work schedules where entry time varies daily.
- Run reliably on a centralized server without requiring user intervention.

## Non-Goals

- Providing a visual web or mobile dashboard for monitoring markings.
- Managing or replacing the BMAquiosque platform itself.
- Supporting work schedules other than Monday-to-Friday.
- Handling holidays automatically (for now).
- Providing payroll, overtime, or HR management features.
- Supporting biometric, geolocation, or CAPTCHA-based time-clock systems.

## Target Users

| User or Actor | Need | Current Pain Point | Priority | Notes |
| --- | --- | --- | --- | --- |
| Employee (small group) | Automatic time-clock registration on BMAquiosque | Frequently forgets to register markings, causing inconsistencies | Primary | Each user manages only themselves; no admin role needed |

## Product Format

| Format | Decision Status | Confirmed By | Notes |
| --- | --- | --- | --- |
| Backend service / server automation | Selected | User | Runs as background service on centralized server, with internal scheduler, no UI, minimal API for future use, Discord bot for all user interaction |

## Product Format Recommendation

| Option | Fit | Advantages | Disadvantages | MVP Viability | Decision |
| --- | --- | --- | --- | --- | --- |
| Backend service / server automation | High | Natural for scheduled processes; centralized multi-user management; scalable; no dependency on user machines | Requires server infrastructure; users need Discord for interaction | High | Selected |
| Discord bot only (all logic in bot) | Medium | Single interaction channel; simpler architecture | Less flexibility for future API expansion; browser automation still needed server-side | Medium | Rejected |
| CLI tool + system scheduler | Low | Simple to build | Each user must run it locally; no centralized management; hard to maintain for a group | Low | Rejected |

Recommendation rationale:

- A backend service is the most natural fit for a process that runs on a fixed schedule (every 30 minutes) without user interaction.
- Centralized server allows managing all users from one place.
- Discord bot integration provides a familiar, zero-setup interaction channel for users.
- Future evolution (API, web dashboard) is easier from a backend service base.

## Proposed Solution

Auto Time Marking is a server-side automation service that:

1. Runs a scheduler that activates every 30 minutes between 05:00 and 22:00, Monday to Friday.
2. For each active user, checks the BMAquiosque platform via automated browser interaction to determine which markings have already been made that day.
3. Based on the markings already done and the user's individual configuration (maximum entry time, lunch rules, time variation), calculates which markings are still needed and when they should be registered.
4. Registers the pending markings on BMAquiosque by automating browser navigation (simulating a real user).
5. Sends Discord notifications for every automatic marking and for any failures.
6. Stores each user's BMAquiosque credentials securely on the server.
7. All user interaction (registration, credential setup, configuration, pause/resume) happens through a Discord bot.

## Full Product Vision

The complete Auto Time Marking product should evolve to include:

- **Core automation**: Reliable daily time-clock automation with intelligent scheduling.
- **Discord bot interface**: Full user lifecycle management (registration, configuration, pause, resume, status check) through Discord commands.
- **Notification system**: Real-time Discord notifications for markings, errors, and system status.
- **Logging and history**: Complete audit trail of all actions, markings, errors, and retries.
- **Holiday management**: Configurable holiday calendars to skip automation on holidays.
- **Advanced scheduling**: Support for different work schedules, shift patterns, and overtime rules.
- **Admin capabilities**: Optional admin role for managing users and viewing system-wide status.
- **Web dashboard**: Optional visual interface for monitoring and configuration.
- **API layer**: RESTful API for programmatic access and third-party integrations.

## Main Capabilities

| ID | Capability | Purpose | Priority | Status |
| --- | --- | --- | --- | --- |
| CAP-001 | Time-Clock Automation Engine | Check existing markings and register pending ones on BMAquiosque to complete the 8h45 workday | Core | Confirmed |
| CAP-002 | Scheduling System | Run the automation every 30 minutes between 05:00 and 22:00, Monday to Friday | Core | Confirmed |
| CAP-003 | Marking Calculation Logic | Calculate when each marking should happen based on existing markings, user config, and workday rules | Core | Confirmed |
| CAP-004 | Discord Bot Interface | Handle user registration, credential setup, configuration, pause/resume, and status via Discord | Core | Confirmed |
| CAP-005 | Notification System | Notify users via Discord about completed markings, failures, and system events | Core | Confirmed |
| CAP-006 | User Management | Individual user accounts with BMAquiosque credentials, personal schedule configuration, and activation status | Core | Confirmed |
| CAP-007 | Logging and Audit | Record all actions, markings, errors, retries, and system events | Supporting | Confirmed |
| CAP-008 | Holiday Management | Configurable holiday calendar to skip automation | Future | Confirmed |
| CAP-009 | Web Dashboard | Visual interface for monitoring and configuration | Future | Confirmed |
| CAP-010 | API Layer | RESTful API for programmatic access | Future | Confirmed |

## Features by Capability

| ID | Capability ID | Feature | Priority | Status |
| --- | --- | --- | --- | --- |
| F-001 | CAP-001 | Automated browser interaction with BMAquiosque (login, navigate, check markings, register marking) | Must | Confirmed |
| F-002 | CAP-001 | Detection of already-registered markings for the current day | Must | Confirmed |
| F-003 | CAP-001 | Automatic registration of pending markings at calculated times | Must | Confirmed |
| F-004 | CAP-001 | Retry mechanism: up to 3 attempts with 5-minute intervals on failure | Must | Confirmed |
| F-005 | CAP-002 | Internal scheduler running every 30 minutes | Must | Confirmed |
| F-006 | CAP-002 | Operating window: 05:00 to 22:00 | Must | Confirmed |
| F-007 | CAP-002 | Operating days: Monday to Friday only | Must | Confirmed |
| F-008 | CAP-003 | Entry marking: register at user-configured maximum time if no manual entry exists | Must | Confirmed |
| F-009 | CAP-003 | Lunch out marking: register at most 6 hours after entry | Must | Confirmed |
| F-010 | CAP-003 | Lunch return marking: register after lunch duration (min 1h, max 2h) | Must | Confirmed |
| F-011 | CAP-003 | Exit marking: calculate based on entry time + lunch duration to complete exactly 8h45 of effective work | Must | Confirmed |
| F-012 | CAP-003 | Recalculate exit time when actual lunch duration differs from minimum (1h) | Must | Confirmed |
| F-013 | CAP-003 | Configurable time variation (jitter) per user (e.g., ±5 min, ±10 min) for natural-looking markings | Must | Confirmed |
| F-014 | CAP-004 | User self-registration via Discord bot | Must | Confirmed |
| F-015 | CAP-004 | BMAquiosque credential setup via Discord bot (secure) | Must | Confirmed |
| F-016 | CAP-004 | Configuration of maximum entry time, lunch preferences, and jitter via Discord bot | Must | Confirmed |
| F-017 | CAP-004 | Pause and resume automation via Discord bot | Must | Confirmed |
| F-018 | CAP-005 | Discord notification on successful automatic marking | Must | Confirmed |
| F-019 | CAP-005 | Discord notification on persistent failure (after 3 retries) | Must | Confirmed |
| F-020 | CAP-006 | Individual user accounts with login/password for the system | Must | Confirmed |
| F-021 | CAP-006 | Secure storage of BMAquiosque credentials per user | Must | Confirmed |
| F-022 | CAP-006 | Individual schedule configuration per user | Must | Confirmed |
| F-023 | CAP-006 | User activation/deactivation (pause/resume) | Must | Confirmed |
| F-024 | CAP-007 | Log all marking actions (success, failure, retry, skip) | Should | Confirmed |
| F-025 | CAP-007 | Log all system events (scheduler runs, errors, user config changes) | Should | Confirmed |
| F-026 | CAP-008 | Configurable holiday calendar | Could | Confirmed |
| F-027 | CAP-009 | Web dashboard for monitoring markings and status | Future | Confirmed |
| F-028 | CAP-010 | RESTful API for user and configuration management | Future | Confirmed |

## User Stories

| ID | User Story | Capability ID | Priority | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| US-001 | As an employee, I want my time-clock entries to be registered automatically so that I don't forget and create inconsistencies in my work records. | CAP-001 | Must | Confirmed | Core value proposition |
| US-002 | As an employee, I want to configure my maximum entry time so that the system registers my entry at the right time if I forget. | CAP-003 | Must | Confirmed | |
| US-003 | As an employee, I want the system to calculate my exit time based on my actual entry and lunch duration so that my workday always totals exactly 8h45. | CAP-003 | Must | Confirmed | |
| US-004 | As an employee, I want to register and configure the system through Discord so that I don't need to learn a new interface. | CAP-004 | Must | Confirmed | |
| US-005 | As an employee, I want to receive Discord notifications when markings are made automatically so that I know my time-clock is being managed. | CAP-005 | Must | Confirmed | |
| US-006 | As an employee, I want to pause the automation when I'm on vacation or leave so that the system doesn't register markings on days I'm not working. | CAP-006 | Must | Confirmed | |
| US-007 | As an employee, I want the system to add time variation to my markings so that they look natural and not robotic. | CAP-003 | Must | Confirmed | |
| US-008 | As an employee, I want the system to retry failed markings and notify me if it still fails so that I can take manual action if needed. | CAP-001, CAP-005 | Must | Confirmed | |
| US-009 | As an employee, I want my BMAquiosque credentials stored securely so that my account is protected. | CAP-006 | Must | Confirmed | |

## Use Cases

| ID | Use Case | Actor | Goal | Related Capability | Notes |
| --- | --- | --- | --- | --- | --- |
| UC-001 | Register via Discord bot | Employee | Create an account and provide BMAquiosque credentials | CAP-004, CAP-006 | Self-service, no admin needed |
| UC-002 | Configure schedule preferences | Employee | Set maximum entry time, lunch preferences, and time jitter | CAP-004, CAP-003 | Via Discord commands |
| UC-003 | Automatic daily marking cycle | System | Check and complete all 4 daily markings for each active user | CAP-001, CAP-002, CAP-003 | Runs autonomously |
| UC-004 | Pause automation | Employee | Temporarily stop automated markings | CAP-004, CAP-006 | For vacations, leaves, etc. |
| UC-005 | Resume automation | Employee | Reactivate automated markings after a pause | CAP-004, CAP-006 | |
| UC-006 | Handle marking failure | System | Retry failed markings and notify user if persistent | CAP-001, CAP-005 | Up to 3 retries, 5 min apart |

## Expected Behaviors

| ID | Trigger or Condition | Expected Behavior | Related Capability | Status |
| --- | --- | --- | --- | --- |
| EB-001 | Scheduler fires (every 30 min, 05:00–22:00, Mon–Fri) | System checks each active user's current markings on BMAquiosque | CAP-002, CAP-001 | Confirmed |
| EB-002 | No entry marking exists and current time ≥ user's max entry time | System registers entry marking with configured jitter | CAP-001, CAP-003 | Confirmed |
| EB-003 | No entry marking exists and current time < user's max entry time | System skips entry marking and waits for next cycle or manual entry | CAP-001, CAP-003 | Confirmed |
| EB-004 | Entry exists, no lunch-out marking, and ≥6h since entry | System registers lunch-out marking with configured jitter | CAP-001, CAP-003 | Confirmed |
| EB-005 | Lunch-out exists, no lunch-return marking, and lunch duration ≥1h | System registers lunch-return marking with configured jitter | CAP-001, CAP-003 | Confirmed |
| EB-006 | Lunch-return exists, no exit marking, and calculated exit time reached | System registers exit marking (entry time + actual lunch duration + 8h45 effective work) with configured jitter | CAP-001, CAP-003 | Confirmed |
| EB-007 | Actual lunch duration > 1h (e.g., 1h30) | Exit time recalculated: entry + 8h45 + actual lunch duration (not just 1h) | CAP-003 | Confirmed |
| EB-008 | Marking attempt fails | System retries up to 3 times, 5 minutes apart | CAP-001 | Confirmed |
| EB-009 | Marking still fails after 3 retries | System stops retrying and sends failure notification via Discord | CAP-001, CAP-005 | Confirmed |
| EB-010 | Automatic marking is successfully registered | System sends success notification via Discord | CAP-005 | Confirmed |
| EB-011 | User is paused | System skips all marking checks for that user | CAP-006, CAP-002 | Confirmed |
| EB-012 | Current time is outside 05:00–22:00 window or day is Sat/Sun | System does not run any marking checks | CAP-002 | Confirmed |
| EB-013 | All 4 markings already exist for the day | System skips user for the rest of the day | CAP-001 | Confirmed |

## Main Flows

### Flow 1: User Registration

1. User sends a registration command to the Discord bot.
2. Bot asks for BMAquiosque credentials (login and password).
3. User provides credentials via Discord DM (private message for security).
4. System stores credentials securely.
5. Bot asks for schedule configuration (max entry time, lunch preferences, jitter).
6. User provides configuration.
7. System creates user account with active status.
8. Bot confirms registration is complete.

### Flow 2: Daily Marking Cycle

1. Scheduler triggers at a 30-minute interval (between 05:00 and 22:00, Mon–Fri).
2. System loads all active users.
3. For each user:
   a. System opens automated browser session and logs into BMAquiosque with user's credentials.
   b. System checks which markings exist for the current day.
   c. System determines which markings are pending.
   d. If a pending marking's trigger condition is met (e.g., max entry time reached, 6h since entry for lunch, etc.):
      - System calculates the marking time with configured jitter.
      - System registers the marking on BMAquiosque.
      - System sends Discord success notification.
      - System logs the action.
   e. If no marking is needed at this time, system skips to next user.
   f. System closes the browser session.
4. Scheduler waits for next 30-minute cycle.

### Flow 3: Pause/Resume Automation

1. User sends a pause command to the Discord bot.
2. System marks the user as paused.
3. Bot confirms the user is paused.
4. While paused, the scheduler skips all marking checks for this user.
5. User sends a resume command.
6. System marks the user as active.
7. Bot confirms the user is active again.

## Alternative Flows

| ID | Scenario | Flow or Behavior | Status |
| --- | --- | --- | --- |
| AF-001 | User manually registers some markings before the system runs | System detects existing markings and only registers the remaining ones, calculating times based on actual markings | Confirmed |
| AF-002 | User manually registers all 4 markings before any automation | System detects all markings are complete and skips the user for the day | Confirmed |
| AF-003 | User registers entry manually but forgets the rest | System uses the manual entry time as the base for calculating lunch and exit times | Confirmed |
| AF-004 | User's lunch takes 1h30 instead of 1h | System uses actual lunch duration (1h30) and recalculates exit to maintain 8h45 effective work | Confirmed |
| AF-005 | User changes configuration while automation is already running for the day | New configuration applies from the next scheduler cycle; already-registered markings are not changed | Confirmed |

## Error and Empty States

| ID | Scenario | Expected User-Facing Behavior | Status |
| --- | --- | --- | --- |
| ES-001 | BMAquiosque is unreachable or times out | System retries up to 3 times (5 min apart), then sends Discord failure notification | Confirmed |
| ES-002 | BMAquiosque login fails (wrong credentials) | System sends Discord notification asking user to update credentials; does not retry with same credentials | Confirmed |
| ES-003 | BMAquiosque changes its UI/structure | System fails to interact, sends Discord notification about unexpected page structure | Confirmed |
| ES-004 | User has no markings and max entry time has not been reached | System waits; no action taken until the condition is met | Confirmed |
| ES-005 | Scheduler fails to start or crashes | System logs the error; if Discord bot is operational, sends notification to affected users | Confirmed |
| ES-006 | User tries to register with invalid BMAquiosque credentials | Bot informs that credentials could not be validated and asks user to try again | Confirmed |
| ES-007 | User tries to configure an invalid max entry time (e.g., before 05:00 or after 22:00) | Bot rejects the configuration and explains valid range | Confirmed |

## Business Rules

| ID | Rule | Source | Status |
| --- | --- | --- | --- |
| BR-001 | A complete workday consists of exactly 8 hours and 45 minutes of effective work time | User | Confirmed |
| BR-002 | Each workday requires exactly 4 markings: entry, lunch-out, lunch-return, exit | User | Confirmed |
| BR-003 | Lunch break minimum duration is 1 hour | User | Confirmed |
| BR-004 | Lunch break maximum duration is 2 hours | User | Confirmed |
| BR-005 | Lunch-out must happen at most 6 hours after entry | User | Confirmed |
| BR-006 | Exit time = entry time + effective work time (8h45) + actual lunch duration | User | Confirmed |
| BR-007 | Total time at workplace = 8h45 + actual lunch duration (minimum 9h45, maximum 10h45) | Derived from BR-001, BR-003, BR-004 | Confirmed |
| BR-008 | System operates only Monday to Friday | User | Confirmed |
| BR-009 | System operates only between 05:00 and 22:00 | User | Confirmed |
| BR-010 | If no manual entry exists by the user's configured max entry time, system registers entry automatically | User | Confirmed |
| BR-011 | On failure, system retries up to 3 times with 5-minute intervals | User | Confirmed |
| BR-012 | Each marking must include configurable time jitter (variation) to appear natural | User | Confirmed |
| BR-013 | Holidays are not managed automatically (for now); system runs on all weekdays | User | Confirmed |

## Validation Rules

| ID | Input or Condition | Rule | Error Behavior | Status |
| --- | --- | --- | --- | --- |
| VR-001 | Max entry time | Must be between 05:00 and 22:00 | Discord bot rejects configuration | Confirmed |
| VR-002 | Time jitter configuration | Must be a positive number (minutes) | Discord bot rejects invalid value | Confirmed |
| VR-003 | BMAquiosque credentials | Must be non-empty login and password | Discord bot asks user to provide valid credentials | Confirmed |
| VR-004 | Lunch duration | Must be between 1h and 2h | System enforces min/max bounds | Confirmed |
| VR-005 | Marking registration | Must not register a marking that already exists for that type on the same day | System skips already-existing markings | Confirmed |

## Permissions and Access Rules

| ID | Actor or Role | Permission or Restriction | Status |
| --- | --- | --- | --- |
| AR-001 | Employee (registered user) | Can register, configure their own settings, pause/resume their own automation, view their own status | Confirmed |
| AR-002 | Employee (registered user) | Cannot view, modify, or manage other users' settings or markings | Confirmed |
| AR-003 | System (automation engine) | Can access BMAquiosque on behalf of each user using their stored credentials | Confirmed |
| AR-004 | Unregistered Discord user | Cannot use bot commands until registered | Confirmed |

## Constraints

- The system depends on BMAquiosque's web interface remaining accessible and structurally stable.
- Browser automation on the server requires a headless browser environment.
- BMAquiosque credentials must be stored securely (encrypted at rest).
- Discord bot requires a Discord server where users are members.
- The system currently does not handle holidays; users must pause manually on holidays.
- Network connectivity is required between the server and BMAquiosque.

## Out of Scope

- Holiday calendar management (deferred to future).
- Visual web or mobile dashboard (deferred to future).
- RESTful API for external integrations (deferred to future).
- Admin role and multi-tenant management.
- Overtime calculation or HR management features.
- Support for time-clock systems other than BMAquiosque.
- Biometric, geolocation, or CAPTCHA handling.
- Mobile app.
- Detailed visual design or branding.

## Assumptions

- BMAquiosque's web interface does not use CAPTCHA, biometric, or geolocation validation for marking registration.
- BMAquiosque does not limit the number of markings per day or block automated access.
- BMAquiosque's web interface is accessible via headless browser automation.
- All users in the group work Monday to Friday with flexible hours.
- All users have BMAquiosque accounts with valid credentials.
- All users have Discord accounts and are members of a shared Discord server.
- The server hosting the system has reliable internet connectivity.

## Open Questions

| Question | Critical Before MVP? | Impact | Owner or Next Step |
| --- | --- | --- | --- |
| What is the exact URL and login flow of BMAquiosque? | Yes | Needed for browser automation implementation | User to provide during implementation |
| Is the Discord bot hosted on a specific server, or should users interact via DM? | No | Affects bot setup but not core behavior | Decide during implementation |
| Should the system validate BMAquiosque credentials at registration time by attempting a test login? | No | UX improvement but not critical for core function | Decide during implementation |
| What happens if a user's lunch exceeds the 2h maximum? | No | Edge case; can be handled as assumption initially | Decide during implementation |
| Should logs be accessible to users via Discord commands (e.g., "show my history")? | No | Nice-to-have feature, not needed for core automation | Decide during future iteration |

## Full Product Completeness Checklist

- [x] Main problem is clearly defined.
- [x] Product goal is clearly defined.
- [x] Target users are identified.
- [x] Product format is selected or explicitly pending.
- [x] Product format recommendation was reviewed by the user.
- [x] Main capabilities are mapped.
- [x] Core behavior is documented.
- [x] Essential business rules are documented.
- [x] Important constraints are documented.
- [x] Critical open questions are resolved before MVP delimitation.

## Next Step: MVP Delimitation

- Readiness: Completed
- MVP PRD: docs/product/auto-time-marking/mvp-prd.md
