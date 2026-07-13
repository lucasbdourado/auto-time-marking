# Project Discovery

## Purpose

This document records the initial project discovery for Auto Time Marking. It serves as the required input for `project-structure` and all subsequent Harness workflows. It captures the project scenario, location, available sources, known technologies, scope boundaries, and open questions identified during the discovery interview.

## Discovery Status

Status: Confirmed

Last updated: 2026-07-13

## Project Scenario

Scenario: Greenfield / No Codebase Available

Codebase status: Not yet created

Next workflow mode: Planning/conceptual analysis

Evidence basis:

- Codebase evidence: None. No implementation code exists yet.
- User-provided context: User confirmed greenfield project during discovery interview.
- Documents or planning sources: Full Product PRD and MVP PRD exist and are confirmed.

## Discovery Interview

Record the user-provided answers gathered before generating this document.

- Project scenario confirmed by user: Greenfield / No Codebase Available
- Project location provided by user: `c:\Users\lucas.dourado\IdeaProjects\auto-time-marking`
- Codebase availability confirmed by user: Not yet created
- Project purpose provided by user: Extracted from existing PRDs (see Available Sources)
- Existing documentation provided by user: Full Product PRD and MVP PRD at `docs/product/auto-time-marking/`
- Related repositories, systems, or integrations provided by user: None
- Greenfield objective, scope, constraints, deliverables, and open decisions provided by user: Mandatory technologies — Java, React, Discord. Out of scope for analysis — `.agents` directory.

## Project Location

- Local path: `c:\Users\lucas.dourado\IdeaProjects\auto-time-marking`
- Remote repository URL: Not provided
- Storage location: Local workspace confirmed by user
- Availability: Available (workspace exists, no implementation code yet)

## Project Summary

Auto Time Marking is a backend automation service that automatically registers time-clock entries (ponto) on the BMAquiosque platform on behalf of its users. The system runs on a centralized server, checks for existing markings every 30 minutes between 05:00 and 22:00 Monday to Friday, and completes the remaining markings needed to fulfill an 8-hour-45-minute workday. The MVP focuses on single-user automation without Discord bot or notifications, validating the core browser automation and marking calculation engine. The full product vision includes multi-user support, Discord bot for all user interaction, notifications, holiday management, web dashboard, and API layer.

## Business Context

- Domain: Workforce time management / time-clock automation.
- Users: Small group of employees with flexible-hours work arrangements.
- Business process: Automated registration of four daily time-clock entries (entry, lunch-out, lunch-return, exit) on the BMAquiosque platform.
- Problem: Users frequently forget to register their time-clock entries, causing inconsistencies in work hour records, payroll issues, and manual correction efforts.
- Target platform: BMAquiosque (external web-based time-clock system).

## Available Sources

| Source | Location | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Full Product PRD | `docs/product/auto-time-marking/full-product-prd.md` | Product requirements | Confirmed | Complete product vision with all capabilities, features, user stories, business rules, and flows |
| MVP PRD | `docs/product/auto-time-marking/mvp-prd.md` | Product requirements | Confirmed | Single-user automation MVP scope with acceptance criteria and phased evolution plan |

## Greenfield Context

- Project objective: Automate time-clock registration on BMAquiosque to eliminate forgotten markings and ensure consistent 8h45 workday records.
- Business domain: Workforce time management.
- Initial scope: Single-user automation (MVP Phase 1), followed by Discord bot + multi-user (Phase 2), then dashboard + API (Phase 3+).
- Expected deliverables: Backend automation service with scheduler, browser automation engine, marking calculation logic, retry mechanism, and file-based logging (MVP). Discord bot, multi-user support, and notifications in Phase 2.
- Known requirements: Documented in Full Product PRD and MVP PRD. MVP has 16 features across 4 capabilities (CAP-001, CAP-002, CAP-003, CAP-007).
- Constraints: Depends on BMAquiosque web interface remaining accessible and not using CAPTCHA/biometric/geolocation. Requires headless browser on server. Credentials must be stored securely.
- Mandatory technologies explicitly provided by the user: Java, React, Discord.
- Prohibited technologies explicitly provided by the user: None.
- Decisions still open: See Open Questions section.
- Product context status: Existing source provided (Full Product PRD and MVP PRD, both confirmed).
- Notes for planning/conceptual analysis: PRDs are confirmed and complete. MVP is well-defined with clear scope, acceptance criteria, and phased evolution plan. `project-structure` must produce a conceptual/proposed structure and must not attempt to analyze nonexistent source files.

## Repositories and Related Systems

No related repositories, dependency repositories, microservices, or external modules were identified. The project is self-contained.

## Main Repository

- Name: auto-time-marking
- Location: `c:\Users\lucas.dourado\IdeaProjects\auto-time-marking`
- Availability: Workspace exists, no implementation code yet
- Notes: Contains `.agents` configuration (out of scope for analysis), `docs/product/` with PRDs, and `.git` initialized.

## Related Repositories

No related repositories identified.

## Microservices

No microservices identified. The MVP is a single backend service.

## Internal Libraries or Dependencies

No internal libraries or dependencies identified.

## External Integrations

| Integration | Purpose | Criticality | Notes |
| --- | --- | --- | --- |
| BMAquiosque | Target platform for time-clock registration via browser automation | Critical | External web system. Exact URL, login flow, and page structure to be provided during implementation. |
| Discord | User interaction channel (registration, configuration, notifications) | Critical (Phase 2+) | Deferred from MVP. Required for full product. |

## Known Technologies

| Technology | Area | Status | Source | Notes |
| --- | --- | --- | --- | --- |
| Java | Backend / Runtime | User constraint | User interview | Mandatory. Explicitly required by the user. |
| React | Frontend | User constraint | User interview | Mandatory. Explicitly required by the user. Likely for future web dashboard (Phase 3+). |
| Discord | User interaction channel | User constraint | User interview, PRD | Mandatory. Explicitly required by the user. Discord bot for Phase 2+. |
| Headless browser automation | Browser automation | Pending confirmation | PRD | Required for BMAquiosque interaction. Specific library (Playwright, Selenium, etc.) not yet decided. |

## Analysis Scope

- Conceptual/proposed project structure based on PRD requirements.
- MVP capabilities: Time-Clock Automation Engine, Scheduling System, Marking Calculation Logic, Logging and Audit.
- Marking calculation rules and business rules documented in PRDs.
- Browser automation interaction model with BMAquiosque.
- Single-user configuration approach for MVP.
- Phased evolution path (MVP → Phase 2 → Phase 3+).

## Out of Scope

- `.agents` directory (Harness configuration, not part of the project).
- Holiday management (deferred to future phases).
- Web dashboard (deferred to Phase 3+, but React is a user-confirmed technology for it).
- RESTful API (deferred to Phase 3+).
- Admin role and multi-tenant management.
- Overtime calculation or HR management features.
- Support for time-clock systems other than BMAquiosque.
- Biometric, geolocation, or CAPTCHA handling.
- Mobile app.

## Existing Documentation

| Document | Location | Type | Relevance | Notes |
| --- | --- | --- | --- | --- |
| Full Product PRD | `docs/product/auto-time-marking/full-product-prd.md` | Product requirements | High | Complete product vision, confirmed |
| MVP PRD | `docs/product/auto-time-marking/mvp-prd.md` | Product requirements | High | MVP scope and acceptance criteria, confirmed |

## Open Questions

| Question | Source | Impact | Owner or Next Step |
| --- | --- | --- | --- |
| What is the exact URL and login flow of BMAquiosque? | MVP PRD | Critical for browser automation implementation | User to provide during implementation |
| What is the exact page structure for checking and registering markings on BMAquiosque? | MVP PRD | Critical for browser automation selectors | User to provide or discover during implementation |
| Should the config file format be YAML, JSON, or .env? | MVP PRD | Low impact; affects developer experience | Decide during technology definition |
| What happens when the service is stopped mid-day and restarted? | MVP PRD | Edge case; assumed re-check markings on restart | Decide during technology definition or tech spec |
| What specific Java frameworks or libraries should be used (e.g., Spring Boot, Quarkus, Micronaut)? | Discovery | Impacts architecture and project structure | Decide during technology definition |
| Which browser automation library should be used with Java (e.g., Playwright for Java, Selenium)? | Discovery, PRD | Critical for BMAquiosque interaction | Decide during technology definition |
| Is the Discord bot hosted on a specific server, or should users interact via DM? | Full Product PRD | Affects bot setup in Phase 2 | Decide during Phase 2 planning |

## Inputs for the Next Step

`project-structure` should use this discovery document as its primary input.

Since this is a Greenfield / No Codebase Available project, `project-structure` must produce a **conceptual/proposed structure** and must not attempt to analyze nonexistent source files or repositories.

The proposed structure should be based on:
- MVP PRD scope (single-user automation, 4 capabilities, 16 features).
- User-confirmed technologies: Java (backend), React (frontend, future), Discord (interaction channel, Phase 2+).
- Phased evolution: MVP → Phase 2 (Discord + multi-user) → Phase 3+ (dashboard + API).

Deeper planning, technology definition, tech specs, and design docs should use the confirmed PRDs as primary product-context sources.

## Notes for `project-structure`

- The project is greenfield. No source code, build files, or configuration files exist yet.
- Java is the confirmed backend technology. The proposed structure should follow Java project conventions.
- React is confirmed for the frontend (future web dashboard). The proposed structure may include a placeholder or note for a future frontend module.
- The `.agents` directory must be excluded from structural analysis.
- The MVP is a single backend service with no frontend component.
- Browser automation (headless) is a critical technical dependency. The specific library is not yet decided.
- The project should be designed to support multi-user evolution (Phase 2) without requiring a full rewrite.
