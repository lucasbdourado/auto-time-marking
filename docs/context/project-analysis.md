# Project Analysis

## Purpose

This document consolidates the third Harness context workflow for Auto Time Marking. It provides a deeper analysis of requirements, conceptual structure, risks, expected modules, planned dependencies, and open decisions for a greenfield project with no existing codebase. It serves as an input for `technology-definition`, `project-planning`, future ADR work, specs, documentation, development, and bug investigation.

## Analysis Status

Status: Confirmed

Last updated: 2026-07-13

## Analysis Mode

Project scenario: Greenfield / No Codebase Available

Analysis basis: Requirements and conceptual planning

Codebase inspected: No

If no codebase was inspected, explain why: The project is greenfield. No implementation code, build files, configuration files, or test files exist. The workspace contains only `.agents` configuration (out of scope), `.git`, and `docs/` with confirmed PRDs and context documents.

## Source Documents

| Source | Location | Type | Relevance |
| --- | --- | --- | --- |
| Project Discovery | `docs/context/project-discover.md` | Discovery | Primary context for project scenario, scope, technologies, and open questions |
| Project Structure | `docs/context/project-structure.md` | Structure | Primary input for proposed modules, layout, hotspots, and structural decisions |
| Full Product PRD | `docs/product/auto-time-marking/full-product-prd.md` | Product requirements | Full product vision with 10 capabilities, 28 features, and 9 user stories |
| MVP PRD | `docs/product/auto-time-marking/mvp-prd.md` | Product requirements | MVP scope with 4 capabilities, 16 features, 5 user stories, and acceptance criteria |

## Project Overview

Auto Time Marking is a backend automation service that automatically registers time-clock entries (ponto) on the BMAquiosque platform on behalf of its users. It runs on a centralized server and uses a scheduler to check for existing markings and register pending ones to complete an 8h45 workday. The MVP targets single-user automation without Discord integration, validating the core browser automation and marking calculation engine. The full product vision extends to multi-user support, Discord bot for all user interaction, notifications, holiday management, web dashboard, and API layer.

## Business Domain Understanding

- **Domain**: Workforce time management / time-clock automation.
- **Business process**: Automated registration of four daily time-clock entries (entry, lunch-out, lunch-return, exit) on the BMAquiosque platform.
- **Problem**: Users frequently forget to register time-clock entries, causing inconsistencies in work hour records, payroll issues, and manual correction efforts.
- **Users**: Small group of employees with flexible-hours work arrangements who share the same BMAquiosque-based time-clock system.
- **Operational workflow**: Every 30 minutes (05:00–22:00, Mon–Fri), the system checks existing markings per user and registers pending ones using browser automation. A complete workday is exactly 8h45 of effective work time.
- **Target platform**: BMAquiosque — an external web-based time-clock system accessed via browser automation.
- **User interaction**: Discord bot (Phase 2+). In MVP, configuration is via environment variables or configuration file, and monitoring is via log files.

Source: Full Product PRD, MVP PRD, project-discover.md.

## System Responsibilities

The system is responsible for:

- Checking existing time-clock markings on BMAquiosque via browser automation.
- Calculating when each of the four daily markings should be registered, respecting business rules (8h45 effective work, lunch min 1h / max 2h, lunch-out at most 6h after entry, configurable max entry time, jitter).
- Registering pending markings automatically on BMAquiosque.
- Retrying failed marking attempts (up to 3 times, 5 min apart).
- Running on a scheduler within a defined operating window (05:00–22:00, Mon–Fri).
- Logging all actions to files (MVP) and sending Discord notifications (Phase 2+).
- Managing user accounts, credentials, and preferences (Phase 2+).
- Respecting already-registered manual markings.

The system is NOT responsible for:

- Replacing or managing the BMAquiosque platform.
- Handling biometric, geolocation, or CAPTCHA-based time-clock systems.
- Overtime calculation, payroll, or HR management.
- Supporting work schedules other than Monday–Friday.
- Holiday management (deferred, users pause manually).
- Support for time-clock systems other than BMAquiosque.

Source: Full Product PRD (Non-Goals, Out of Scope), MVP PRD (Out of Scope).

## Repository Analysis Summary

No repository was analyzed. The project is greenfield with no implementation code. This analysis is based entirely on requirements, planning documentation, and user-provided context recorded in the discovery and structure documents.

## Greenfield Requirements Analysis

- **Project objective**: Automate time-clock registration on BMAquiosque to eliminate forgotten markings and ensure consistent 8h45 workday records.
- **Business domain**: Workforce time management.
- **Initial scope**: Single-user automation (MVP Phase 1), followed by Discord bot + multi-user (Phase 2), then dashboard + API (Phase 3+).
- **Expected deliverables**:
  - MVP: Backend automation service with scheduler, browser automation engine, marking calculation logic, retry mechanism, single-user configuration, and file-based logging.
  - Phase 2: Discord bot, multi-user support, user management, notifications, pause/resume.
  - Phase 3+: Holiday management, web dashboard (React), REST API.
- **Key requirements**: Documented in Full Product PRD (10 capabilities, 28 features) and MVP PRD (4 capabilities, 16 features, 11 expected behaviors, 11 business rules, 5 validation rules, 16 acceptance criteria).
- **Constraints**:
  - Depends on BMAquiosque web interface remaining accessible and structurally stable.
  - BMAquiosque must not use CAPTCHA, biometric, or geolocation validation.
  - Requires headless browser on server.
  - Credentials must be stored securely (encrypted at rest).
  - Network connectivity required between server and BMAquiosque.
  - Discord server required for Phase 2+ user interaction.
- **Assumptions explicitly provided by the user**:
  - BMAquiosque is automatable via headless browser.
  - BMAquiosque does not limit markings per day or block automated access.
  - All users work Monday to Friday with flexible hours.
  - Server has reliable internet connectivity.
  - Configuration is provided before the system starts (no runtime config changes in MVP).
- **Open decisions**: See Open Questions section.

## Expected Modules and Responsibilities

| Proposed Module or Area | Expected Responsibility | Basis | Status | Notes |
| --- | --- | --- | --- | --- |
| Application entry point | Main class with scheduler bootstrap, configuration loading, validation, and graceful shutdown | MVP PRD CAP-002, MVP-F-016 | Proposed | Depends on Java framework decision |
| Scheduler module | Internal scheduler running every 30 min within the 05:00–22:00 Mon–Fri operating window | MVP PRD CAP-002 (MVP-F-005, MVP-F-006, MVP-F-007) | Proposed | Must enforce operating window and day-of-week rules |
| Browser automation module | Headless browser interaction with BMAquiosque: login, navigate, check markings, register markings, handle page structure | MVP PRD CAP-001 (MVP-F-001, MVP-F-002, MVP-F-003) | Proposed | Critical module; highest external dependency risk. Browser automation library pending decision |
| Marking calculation engine | Calculate timing for entry, lunch-out, lunch-return, exit; enforce business rules (8h45, lunch bounds, 6h rule); apply jitter; recalculate on actual lunch duration | MVP PRD CAP-003 (MVP-F-008 through MVP-F-013) | Proposed | Business rules are well-defined in PRD. High testability priority — pure logic module |
| Retry mechanism | Retry failed marking attempts up to 3 times with 5-minute intervals; escalate to logging/notification on final failure | MVP PRD CAP-001 (MVP-F-004) | Proposed | May be part of browser automation module or a cross-cutting concern |
| Configuration module | Load and validate user configuration from environment variables or config file; provide validated config to other modules | MVP PRD MVP-F-016, MVP-VR-001 through MVP-VR-003 | Proposed | Config format pending decision (YAML, JSON, .env) |
| Logging module | File-based logging of all actions (markings, retries, errors, skips, scheduler runs, system events) | MVP PRD CAP-007 (MVP-F-014, MVP-F-015) | Proposed | Logging library pending decision |
| Discord bot module | User registration, credential setup, configuration, pause/resume, status via Discord commands | Full PRD CAP-004 (F-014 through F-017) | Proposed (Phase 2) | Deferred from MVP |
| Notification module | Discord notifications on successful markings and persistent failures | Full PRD CAP-005 (F-018, F-019) | Proposed (Phase 2) | Deferred from MVP |
| User management module | Individual user accounts, secure credential storage, schedule configuration, activation/deactivation | Full PRD CAP-006 (F-020 through F-023) | Proposed (Phase 2) | Database technology pending decision |
| Holiday management module | Configurable holiday calendar to skip automation | Full PRD CAP-008 (F-026) | Proposed (Phase 3+) | Deferred |
| Web dashboard module | Visual interface for monitoring markings and status | Full PRD CAP-009 (F-027) | Proposed (Phase 3+) | React confirmed as user constraint |
| API layer module | RESTful API for user and configuration management | Full PRD CAP-010 (F-028) | Proposed (Phase 3+) | Deferred |

## Main Modules and Responsibilities

Not applicable — no codebase exists to verify module boundaries. See **Expected Modules and Responsibilities** above for proposed modules derived from requirements.

## Cross-Repository Dependencies

Not applicable. The project is self-contained with no cross-repository relationships.

## Internal Dependencies

The following are expected internal module dependencies based on the proposed structure:

| Dependency | Area | Evidence | Notes |
| --- | --- | --- | --- |
| Scheduler → Browser automation | Core flow | MVP PRD Flow 1 | Scheduler triggers browser automation checks |
| Scheduler → Marking calculation | Core flow | MVP PRD Flow 1 | Scheduler cycle determines when calculations run |
| Browser automation → Marking calculation | Data flow | MVP PRD EB-002 through EB-007 | Browser automation provides existing markings; calculation determines pending ones |
| Browser automation → Retry mechanism | Error handling | MVP PRD MVP-F-004 | Failed browser operations trigger retries |
| Browser automation → Configuration | Credentials | MVP PRD MVP-F-016, MVP-VR-003 | Browser automation needs BMAquiosque credentials |
| Marking calculation → Configuration | User settings | MVP PRD MVP-F-008, MVP-F-013 | Calculation needs max entry time, jitter, lunch preferences |
| All modules → Logging | Cross-cutting | MVP PRD CAP-007 | All modules log their actions |
| Application entry point → Configuration | Bootstrap | MVP PRD MVP-F-016 | Configuration must be loaded and validated at startup |
| Application entry point → Scheduler | Bootstrap | MVP PRD CAP-002 | Entry point starts the scheduler |

Status: Planned. These dependencies are derived from the proposed module structure and PRD flows; they are not verified from code.

## External Integrations

| Integration | Purpose | Evidence | Risk or Criticality | Notes |
| --- | --- | --- | --- | --- |
| BMAquiosque | Target platform for time-clock registration via browser automation | MVP PRD CAP-001, Full PRD F-001 | **Critical** — single point of failure; UI changes break automation | External web system. Exact URL, login flow, and page structure pending user input. No API available — browser automation only. |
| Discord | User interaction channel (registration, configuration, notifications) via Discord bot | Full PRD CAP-004, CAP-005 | **Critical (Phase 2+)** — required for multi-user interaction | Deferred from MVP. User constraint (mandatory technology). |

## Detected Technologies

No technologies were detected from codebase because no codebase exists.

## Technology Evidence

No codebase evidence available.

| Area | Technology | Evidence | Repository | Notes |
| --- | --- | --- | --- | --- |
| — | — | No codebase exists | — | All technologies are user constraints, planned, or pending confirmation |

## Technology Constraints and Pending Recommendations

| Area | Technology or Constraint | Status | Source | Notes |
| --- | --- | --- | --- | --- |
| Backend / Runtime | Java | User constraint | Discovery interview | Mandatory. Explicitly required by the user. |
| Frontend (Phase 3+) | React | User constraint | Discovery interview | Mandatory. For future web dashboard. Not needed for MVP. |
| User interaction (Phase 2+) | Discord | User constraint | Discovery interview, Full PRD | Mandatory. Discord bot for user interaction. Not needed for MVP. |
| Browser automation | Headless browser library (Playwright for Java, Selenium, etc.) | Pending confirmation | MVP PRD CAP-001 | Critical dependency. Library not yet decided. |
| Java framework | Spring Boot, Quarkus, Micronaut, or plain Java | Pending confirmation | Discovery | Impacts project layout, DI, configuration, scheduler. |
| Build tool | Maven or Gradle | Pending confirmation | Java convention | Determines project structure and dependency management. |
| Scheduler | Internal scheduler (implementation TBD) | Pending confirmation | MVP PRD CAP-002 | Approach depends on Java framework choice. |
| Configuration format | YAML, JSON, or .env | Pending confirmation | MVP PRD MVP-F-016 | Low impact; affects developer experience. |
| Logging library | SLF4J + Logback, Log4j2, JUL, or other | Pending confirmation | MVP PRD CAP-007 | File-based logging required. |
| Credential encryption | Encryption approach TBD | Pending confirmation | MVP PRD MVP-VR-003, Full PRD F-021 | Required for secure credential storage. |
| Database (Phase 2+) | PostgreSQL, MySQL, H2, SQLite, or other | Insufficient evidence | Full PRD CAP-006 | Required for multi-user user management. Not needed for MVP (file/config-based). |
| Discord bot library (Phase 2+) | JDA, Discord4J, or other | Insufficient evidence | Full PRD CAP-004 | Deferred. Required for Phase 2. |
| Testing framework | JUnit 5, TestNG, or other | Insufficient evidence | — | Not yet decided. |
| API framework (Phase 3+) | Spring Web, JAX-RS, or other | Insufficient evidence | Full PRD CAP-010 | Deferred. |

## Existing Architectural Decisions

No existing architectural decisions were observed from codebase. The project is greenfield. Future ADR topics are listed in the **Candidate ADRs** section.

## Architectural Patterns Observed

Not applicable — no codebase exists. No architectural patterns were observed.

Recommended patterns to evaluate during `technology-definition` and `tech-spec` (all pending decision):

| Pattern | Area | Basis | Notes |
| --- | --- | --- | --- |
| Modular monolith | System architecture | MVP PRD (single service), evolution to multi-user | Proposed to support MVP simplicity with Phase 2 extensibility |
| Capability-based package organization | Code organization | Proposed structure in project-structure.md | Packages by capability/module rather than by layer |
| Scheduler-driven orchestration | Core flow | MVP PRD CAP-002, Flow 1 | Scheduler triggers the entire marking cycle |
| Strategy/adapter pattern for browser automation | Browser automation | MVP PRD CAP-001 | Isolate BMAquiosque-specific selectors and navigation from automation logic |

Status: All patterns are recommendations for evaluation; none are confirmed.

## Code Organization Patterns

Not applicable — no codebase exists. The proposed project layout in `project-structure.md` follows conventional Java project structure (`src/main/java`, `src/test/java`, `src/main/resources`). Package naming convention and module organization are pending technology decisions.

## Data and Persistence Analysis

**MVP**: No database required. Configuration is file-based or environment-variable-based. State is transient — the system re-checks BMAquiosque markings each cycle, so no local marking state needs to be persisted.

**Phase 2+**: Database required for:
- User accounts and profiles (Full PRD CAP-006).
- Secure credential storage per user (Full PRD F-021).
- Schedule configuration per user (Full PRD F-022).
- Activation/deactivation status (Full PRD F-023).
- Potentially marking history for consultable logs.

Database technology is not yet decided. This is a `technology-definition` input.

**Credential security**: Both MVP and full product require secure handling of BMAquiosque credentials. MVP stores them in config/env; full product stores them encrypted in the database. Encryption approach is pending decision.

Source: MVP PRD MVP-F-016, MVP-VR-003; Full PRD F-021, CAP-006.

## API and Integration Analysis

**MVP**: No REST API or web endpoints. The only external integration is BMAquiosque, accessed via browser automation (not API).

**BMAquiosque integration analysis**:
- Access method: Headless browser automation (login, navigate, check markings, register markings).
- Authentication: Form-based login with user credentials.
- Page interaction: HTML selectors to identify marking status and registration forms.
- Critical unknowns: Exact URL, login flow, page structure, and HTML selectors are not yet provided by the user.
- Risk: Any UI change on BMAquiosque will break the automation. No API fallback.

**Phase 2+**: Discord bot API for user interaction (registration, configuration, notifications). Phase 3+: REST API for programmatic access.

Source: MVP PRD CAP-001, MVP-ES-003; Full PRD CAP-004, CAP-010.

## Security and Authentication Notes

**BMAquiosque authentication**: The system authenticates to BMAquiosque using the user's credentials. Credentials must be stored securely:
- MVP: In configuration file or environment variable. Encryption approach pending decision.
- Full product: Encrypted at rest in database (Full PRD constraint).

**Discord authentication (Phase 2+)**: Discord bot token required. Users interact via Discord commands; bot must handle credential setup via DM for privacy (Full PRD Flow 1, step 3).

**Access control (Phase 2+)**: Users can only manage their own settings and view their own status (Full PRD AR-001, AR-002). Unregistered users cannot use bot commands (Full PRD AR-004).

**No external authentication system**: The system does not implement its own user authentication in MVP. Phase 2+ user accounts are managed via Discord identity.

Source: MVP PRD MVP-VR-003; Full PRD AR-001 through AR-004, constraints.

## Testing Strategy Observed

Not applicable — no test framework, test files, or test structure exist.

**Testing needs identified from requirements**:

| Test Area | Priority | Basis | Notes |
| --- | --- | --- | --- |
| Marking calculation logic | High | MVP PRD CAP-003, MVP-EB-002 through MVP-EB-007 | Pure logic; highly testable with unit tests. Complex business rules with edge cases (variable lunch, jitter, recalculation). |
| Scheduler operating window | Medium | MVP PRD CAP-002, MVP-EB-011 | Verify 05:00–22:00 window, Mon–Fri enforcement, 30-min intervals. |
| Configuration validation | Medium | MVP PRD MVP-VR-001 through MVP-VR-003 | Verify startup fails with clear error on invalid config. |
| Retry mechanism | Medium | MVP PRD MVP-F-004, MVP-EB-008, MVP-EB-009 | Verify retry count, interval, and escalation behavior. |
| Browser automation | High (integration) | MVP PRD CAP-001 | Challenging to test; requires BMAquiosque access or mocked browser sessions. |
| End-to-end marking cycle | High | MVP PRD Flow 1 | Full daily cycle from scheduler trigger through marking registration. |

Testing framework, approach, and strategy are pending `technology-definition`.

## Build, Runtime and Deployment Notes

**Build**: No build tool, dependency management, or CI/CD exists. Build tool (Maven or Gradle) is pending decision. Standard Java build convention expected.

**Runtime**: The service runs as a background process on a server. Requires:
- Java runtime (version pending decision).
- Headless browser runtime (Chromium or equivalent, depending on automation library).
- Network access to BMAquiosque.
- Configuration file or environment variables.

**Deployment**: Strategy pending decision. Options include:
- Executable JAR.
- Docker container (Dockerfile proposed in project-structure.md).
- Systemd service.

**Environment**: Server must support headless browser execution. This may require specific OS packages (e.g., Chromium dependencies on Linux) or a Docker image with browser pre-installed.

Source: Discovery (constraints), project-structure.md (proposed layout).

## Documentation Gaps

| Gap | Impact | Evidence or Reason | Notes |
| --- | --- | --- | --- |
| BMAquiosque URL and login flow | Critical — blocks browser automation implementation | MVP PRD Open Questions | User must provide during implementation |
| BMAquiosque page structure and HTML selectors | Critical — blocks browser automation selectors | MVP PRD Open Questions | User must provide or discover during implementation |
| README and project documentation | Low — standard project documentation | No README exists | Create during implementation |
| Architecture documentation | Medium — no ADRs, no architecture docs | Greenfield; no decisions made yet | Generate during `technology-definition` and ADR work |
| API documentation (Phase 3+) | Low — deferred | Not needed for MVP | Generate when API is implemented |
| Deployment documentation | Medium — needed before production deployment | No deployment strategy defined | Define during `technology-definition` |

## Technical Risks

| Risk | Area | Evidence | Impact | Notes |
| --- | --- | --- | --- | --- |
| BMAquiosque UI changes break automation | Browser automation | MVP PRD MVP-ES-003 | **Critical** — any UI change can break all marking functionality | No API fallback. Selectors and navigation are brittle. Mitigation: isolate selectors, add clear error reporting, design for rapid selector updates. |
| BMAquiosque adds CAPTCHA, biometric, or geolocation | Browser automation | MVP PRD Assumptions | **Critical** — would completely block automation | Assumption-based risk. No mitigation if BMAquiosque adds these protections. |
| BMAquiosque blocks automated access | Browser automation | Full PRD Assumptions | **Critical** — would completely block automation | Assumption-based risk. Behavioral analysis or rate limiting by BMAquiosque could detect automation. |
| Headless browser reliability on server | Runtime | Discovery constraints | **High** — browser crashes, memory leaks, or timeout issues | Headless browsers can be resource-intensive and flaky. Mitigation: proper session management, timeouts, retry mechanism. |
| Single point of failure (single service) | Architecture | MVP PRD | **Medium** — service crash stops all automation | MVP is single-user, so impact is limited. Phase 2+ multi-user amplifies this risk. Mitigation: proper error handling, restart capability, logging. |
| Credential security in MVP config | Security | MVP PRD MVP-VR-003 | **Medium** — credentials in plain text config are a security risk | MVP stores credentials in config/env. Encryption approach pending decision. |
| Jitter and timing accuracy | Marking calculation | MVP PRD MVP-F-013 | **Low** — jitter must produce natural-looking but valid markings | Edge cases where jitter pushes markings outside valid windows. Mitigation: bound jitter within valid ranges. |
| Service restart mid-day | Scheduler | MVP PRD Open Questions | **Low** — system should re-check markings on restart | Assumption in PRD: re-check on restart. Needs confirmation during tech spec. |

## Maintenance Risks

| Risk | Area | Evidence | Impact | Notes |
| --- | --- | --- | --- | --- |
| BMAquiosque selector maintenance | Browser automation | MVP PRD MVP-ES-003 | **High** — ongoing maintenance cost whenever BMAquiosque UI changes | Mitigation: centralize selectors, use clear naming, add tests that detect selector failures early. |
| Browser automation library updates | Dependencies | Discovery (pending decision) | **Medium** — library updates may require code changes | Mitigation: isolate browser automation behind an abstraction layer. |
| Java framework and dependency updates | Dependencies | Discovery (pending decision) | **Medium** — standard dependency maintenance | Mitigation: use well-maintained, LTS-supported frameworks and libraries. |
| Phase 2 migration complexity | Architecture evolution | Full PRD phased evolution | **Medium** — single-user to multi-user migration | MVP architecture should support multi-user evolution without full rewrite. Design for extensibility. |

## Development Guidance

- **Start with marking calculation engine**: This is pure business logic with well-defined rules from the PRD. It has the highest testability and no external dependencies. Build and fully test this module first.
- **Isolate browser automation**: BMAquiosque interaction is the highest-risk module. Isolate it behind a clean interface so that selectors and navigation logic can be updated without affecting other modules.
- **Design for multi-user from the start**: Even though MVP is single-user, structure the code so that the user context (credentials, configuration, preferences) is passed as parameters rather than hardcoded. This makes Phase 2 multi-user evolution straightforward.
- **Centralize BMAquiosque selectors**: Keep all HTML selectors and page navigation logic in a single, well-documented location. This minimizes maintenance effort when BMAquiosque changes.
- **Implement retry as a reusable concern**: The retry mechanism (3 attempts, 5 min apart) will be needed for all browser interactions. Implement it as a reusable utility.
- **Validate configuration at startup**: The system should fail fast with clear error messages if configuration is invalid (MVP PRD MVP-VR-001 through MVP-VR-003).
- **Log everything**: File-based logging is the only monitoring mechanism in MVP. Log all scheduler runs, marking checks, calculations, registrations, retries, skips, and errors with sufficient detail for debugging.

## Bug Investigation Guidance

- **Marking not registered**: Check scheduler logs to confirm the cycle ran. Check browser automation logs for login or navigation failures. Check marking calculation logs to confirm the trigger condition was met. Check retry logs for transient failures.
- **Wrong marking time**: Check marking calculation logic against the business rules (MVP PRD CAP-003). Verify the actual entry time used as base, actual lunch duration, and jitter applied.
- **Login failure**: Check credentials in configuration. Check BMAquiosque accessibility (network, URL). Check browser automation logs for page structure changes.
- **Scheduler not running**: Check application startup logs for configuration validation errors. Check scheduler configuration (interval, operating window, day-of-week). Check for JVM crashes or out-of-memory errors.
- **Retry exhaustion**: Check all 3 retry attempt logs. Identify the root cause of the failure (network timeout, login failure, selector mismatch, page structure change).

## Candidate ADRs

No existing architectural decisions were observed from codebase. The following are future ADR topics with status `Pending decision`:

| Candidate ADR | Observed Decision | Evidence | Reason |
| --- | --- | --- | --- |
| ADR: Java Framework Selection | Pending decision | Discovery, project-structure.md (open decision) | Impacts project layout, dependency injection, configuration, scheduler implementation, and overall architecture. Spring Boot, Quarkus, Micronaut, or plain Java. |
| ADR: Build Tool Selection | Pending decision | Project-structure.md (open decision) | Determines dependency management, build lifecycle, and project conventions. Maven or Gradle. |
| ADR: Browser Automation Library | Pending decision | MVP PRD CAP-001, Discovery | Critical MVP dependency. Playwright for Java, Selenium, or other. Impacts browser interaction API, test approach, and runtime requirements. |
| ADR: Configuration Format | Pending decision | MVP PRD MVP-F-016, Discovery | YAML, JSON, .env, or Spring properties. Low impact but affects developer experience and library choices. |
| ADR: Credential Encryption Strategy | Pending decision | MVP PRD MVP-VR-003, Full PRD F-021 | How BMAquiosque credentials are stored securely. Impacts MVP config and Phase 2 database storage. |
| ADR: Database Technology (Phase 2+) | Pending decision | Full PRD CAP-006 | PostgreSQL, MySQL, H2, SQLite, or other. Required for multi-user user management. Not needed for MVP. |
| ADR: Deployment Strategy | Pending decision | Project-structure.md (open decision) | Executable JAR, Docker container, or systemd service. Impacts build pipeline and runtime environment. |
| ADR: Modular vs. Layered Architecture | Pending decision | Project-structure.md (open decision) | Package organization by capability/module vs. by layer. Impacts code organization and Phase 2 extensibility. |
| ADR: Testing Strategy and Framework | Pending decision | — | JUnit 5 or TestNG, mocking approach, integration test strategy for browser automation. |
| ADR: Logging Framework | Pending decision | MVP PRD CAP-007 | SLF4J + Logback, Log4j2, or JUL. File-based logging required. |

## Inputs for `technology-definition`

### User Constraints (Mandatory)

- **Java**: Backend / runtime. Explicitly required by the user.
- **React**: Frontend (Phase 3+ web dashboard). Explicitly required by the user. Not needed for MVP.
- **Discord**: User interaction channel (Phase 2+ bot). Explicitly required by the user. Not needed for MVP.

### Technology Gaps Requiring Decisions

| Area | Why | Priority | Options to Evaluate |
| --- | --- | --- | --- |
| Java framework | Determines project structure, DI, configuration, scheduler, and web layer | Critical for MVP | Spring Boot, Quarkus, Micronaut, plain Java |
| Build tool | Determines dependency management and build lifecycle | Critical for MVP | Maven, Gradle |
| Browser automation library | Required for BMAquiosque interaction — core MVP functionality | Critical for MVP | Playwright for Java, Selenium |
| Configuration format | Needed for single-user config in MVP | Medium | YAML, JSON, .env, Spring properties |
| Credential encryption | Needed for secure credential storage | Medium | Jasypt, Spring Security Crypto, Java Keystore, custom |
| Logging library | File-based logging required for MVP | Medium | SLF4J + Logback, Log4j2 |
| Testing framework | Needed for unit and integration tests | Medium | JUnit 5, TestNG |
| Deployment strategy | Needed before production deployment | Low (not blocking MVP development) | Docker, executable JAR, systemd |
| Database (Phase 2+) | Required for multi-user support | Not needed for MVP | PostgreSQL, MySQL, H2, SQLite |
| Discord bot library (Phase 2+) | Required for Discord integration | Not needed for MVP | JDA, Discord4J |

### Context for Technology Decisions

- MVP is a single backend service with no frontend, no API, and no database.
- The system must run a headless browser on a server, which constrains runtime environment.
- The architecture should support multi-user evolution (Phase 2) without a full rewrite.
- The marking calculation engine is pure business logic with no external dependencies — framework choice does not affect it.
- Browser automation is the highest-risk area and the library choice significantly impacts implementation approach, reliability, and testability.

## Inputs for `project-planning`

### Current Capabilities

MVP has 4 confirmed capabilities (CAP-001, CAP-002, CAP-003, CAP-007) covering 16 features and 5 user stories. Full product has 10 capabilities, 28 features, and 9 user stories.

### Constraints

- External dependency on BMAquiosque web interface (URL, login flow, and page structure unknown).
- Headless browser runtime requirement on server.
- Single-user in MVP; multi-user in Phase 2.
- No Discord integration in MVP.
- No database in MVP.

### Risks for Planning

- BMAquiosque UI stability is the primary technical risk. Planning should account for selector maintenance and rapid-update capability.
- The unknown BMAquiosque page structure may block or delay browser automation implementation.
- Multi-user evolution (Phase 2) needs to be planned from the start to avoid architecture rework.

### Gaps

- BMAquiosque URL, login flow, and page structure are critical unknowns.
- Technology decisions are pending (Java framework, browser automation library, etc.).
- Deployment strategy is not yet defined.

### Sensitive Areas

- Credential handling (security).
- Browser automation reliability (external dependency).
- Marking calculation accuracy (business rule correctness, edge cases with jitter and variable lunch).

### Planning Questions

- Should MVP planning include the BMAquiosque page structure discovery as a research/spike task?
- What is the expected MVP delivery timeline?
- Are there any additional constraints on the server environment (OS, available resources, network restrictions)?
- Should the MVP architecture explicitly prepare for multi-user, or should this be handled as a Phase 2 refactoring concern?

## Open Questions

| Question | Source | Impact | Owner or Next Step |
| --- | --- | --- | --- |
| What is the exact URL and login flow of BMAquiosque? | MVP PRD, Discovery | Critical — blocks browser automation implementation | User to provide during implementation |
| What is the exact page structure for checking and registering markings on BMAquiosque? | MVP PRD, Discovery | Critical — blocks browser automation selectors | User to provide or discover during implementation |
| Should the config file format be YAML, JSON, or .env? | MVP PRD, Discovery | Low — affects developer experience | Decide during `technology-definition` |
| What happens when the service is stopped mid-day and restarted? | MVP PRD | Low — assumed re-check markings on restart | Confirm during tech spec |
| What specific Java framework should be used? | Discovery | High — impacts architecture and project structure | Decide during `technology-definition` |
| Which browser automation library should be used with Java? | Discovery, MVP PRD | Critical — core MVP dependency | Decide during `technology-definition` |
| What encryption approach should be used for credentials? | MVP PRD, Full PRD | Medium — security concern | Decide during `technology-definition` |
| Is the Discord bot hosted on a specific server, or should users interact via DM? | Full PRD | Low (Phase 2) — affects bot setup | Decide during Phase 2 planning |
| What is the target server environment (OS, resources)? | — | Medium — affects deployment and browser automation | User to provide |

## Notes for Next Steps

- `docs/context/project-analysis.md` is now complete and serves as an input for `technology-definition`, `project-planning`, future ADR work, specs, documentation, development, and bug investigation.
- The next recommended Harness step is **`project-planning`** (using PRDs and this analysis as inputs) or **`technology-definition`** (if technology decisions should be made before planning). Both can be initiated; `technology-definition` is typically done after planning identifies which features need technology decisions, but can also precede planning when critical technology choices affect the planning structure.
- The **BMAquiosque page structure** is a critical unknown that should be addressed as early as possible — either as a research/spike task during planning or as a prerequisite for the browser automation tech spec.
- All proposed modules, patterns, dependencies, and recommendations in this document are conceptual and pending confirmation through downstream Harness workflows.
