# Project Structure

## Purpose

This document records the structural analysis of the Auto Time Marking project. Since the project is greenfield with no codebase available, this document presents a conceptual/proposed structure based on the confirmed PRDs and discovery interview. It serves as the required input for `project-analysis` and all subsequent Harness workflows.

## Structure Analysis Status

Status: Confirmed

Last updated: 2026-07-13

## Structure Mode

Project scenario: Greenfield / No Codebase Available

Structure basis: Conceptual/proposed structure from requirements

Codebase inspected: No

If no codebase was inspected, explain why: The project is greenfield. No implementation code, build files, or configuration files exist. The workspace contains only `.agents` configuration, `.git`, and `docs/product/` with confirmed PRDs.

## Source Documents

| Source | Location | Type | Relevance |
| --- | --- | --- | --- |
| Project Discovery | `docs/context/project-discover.md` | Discovery | Primary input for this document |
| Full Product PRD | `docs/product/auto-time-marking/full-product-prd.md` | Product requirements | Full product vision with 10 capabilities and 28 features |
| MVP PRD | `docs/product/auto-time-marking/mvp-prd.md` | Product requirements | MVP scope with 4 capabilities and 16 features |

## Repositories Overview

No repository was required or inspected. The project is greenfield with no codebase.

The workspace at `c:\Users\lucas.dourado\IdeaProjects\auto-time-marking` currently contains:

| Item | Type | Purpose |
| --- | --- | --- |
| `.agents/` | Directory | Harness agent configuration (out of scope for analysis) |
| `.git/` | Directory | Git repository initialization |
| `.gitmodules` | File | Git submodule configuration |
| `docs/` | Directory | Product documentation (PRDs) and context documents |

No source code, build files, dependency management files, configuration files, or test files exist.

## Repository Relationship Map

The project is a self-contained single repository. No related repositories, microservices, or dependency repositories were identified in discovery.

Future evolution (Phase 2+) may introduce separate concerns (Discord bot, frontend dashboard), but discovery does not record any separate repositories for these.

## Main Repository

### Responsibility

Backend automation service that automatically registers time-clock entries on BMAquiosque for users. MVP is single-user. Full product supports multi-user via Discord bot.

### Location

- Local path: `c:\Users\lucas.dourado\IdeaProjects\auto-time-marking`
- Remote URL: Not provided
- Availability: Workspace exists, no implementation code

### Detected Technologies

No codebase evidence available. No technologies were detected from code inspection.

| Area | Technology | Evidence | Status |
| --- | --- | --- | --- |
| Backend / Runtime | Java | No codebase evidence | User constraint (mandatory) |
| Frontend | React | No codebase evidence | User constraint (mandatory, Phase 3+ dashboard) |
| User interaction | Discord | No codebase evidence | User constraint (mandatory, Phase 2+ bot) |
| Browser automation | Headless browser library | No codebase evidence | Pending confirmation (library not yet decided) |

### Important Files

No implementation files exist. The following files are present in the workspace:

| File | Purpose | Notes |
| --- | --- | --- |
| `.gitmodules` | Git submodule references | Present in workspace root |
| `docs/product/auto-time-marking/full-product-prd.md` | Full product requirements | Confirmed |
| `docs/product/auto-time-marking/mvp-prd.md` | MVP requirements | Confirmed |
| `docs/context/project-discover.md` | Project discovery | Confirmed |

### Directory Overview

No implementation directories exist. See **Proposed Greenfield Structure** for conceptual directory mapping.

### Entry Points

No entry points detected. See **Proposed Greenfield Structure** for expected entry points.

### Build and Dependency Management

No build tools, dependency files, lockfiles, scripts, or package managers found. These will be established during technology definition (e.g., Maven `pom.xml` or Gradle `build.gradle` for Java).

### Configuration Files

No configuration files found. MVP PRD specifies that configuration will be via environment variables or a configuration file (format not yet decided — YAML, JSON, or .env).

### Test Structure

No test directories or frameworks found. Test strategy will be defined during technology definition and tech spec.

### Documentation Structure

| Directory or File | Purpose | Notes |
| --- | --- | --- |
| `docs/product/auto-time-marking/` | Product requirements (PRDs) | Contains confirmed Full Product and MVP PRDs |
| `docs/context/` | Harness context workflow documents | Contains project-discover.md and this document |

### Development Hotspots

Not applicable — no codebase exists. Expected hotspots for future development based on PRD:

| Expected Hotspot | Reason | Basis |
| --- | --- | --- |
| Browser automation module | Core interaction with BMAquiosque; most change-prone due to external UI dependency | MVP PRD CAP-001 |
| Marking calculation engine | Business rule complexity; variable lunch, jitter, recalculation | MVP PRD CAP-003 |
| Scheduler configuration | Operating window, interval, day-of-week rules | MVP PRD CAP-002 |
| User configuration | Single-user config (MVP), multi-user config (Phase 2) | MVP PRD MVP-F-016, Full PRD CAP-006 |

### Bug Investigation Hotspots

Not applicable — no codebase exists. Expected investigation hotspots:

| Expected Hotspot | Reason | Basis |
| --- | --- | --- |
| Browser automation selectors | BMAquiosque UI changes will break selectors | MVP PRD MVP-ES-003 |
| Retry and error handling | Transient failures, timeout behavior | MVP PRD MVP-F-004 |
| Marking time calculations | Edge cases with lunch duration, jitter ranges | MVP PRD MVP-EB-007 |
| Credential handling | Login failures, credential storage | MVP PRD MVP-ES-002 |

## Proposed Greenfield Structure

> **Note**: This structure is conceptual/proposed and was NOT detected from codebase. All items are derived from the confirmed MVP PRD and Full Product PRD.

### MVP Proposed Structure (Phase 1)

| Area or Proposed Module | Expected Responsibility | Basis | Status | Open Questions |
| --- | --- | --- | --- | --- |
| Application entry point | Main class with scheduler bootstrap and configuration loading | MVP PRD CAP-002 | Proposed | Java framework (Spring Boot, Quarkus, etc.) not yet decided |
| Scheduler module | Internal scheduler running every 30 min, operating window enforcement (05:00–22:00, Mon–Fri) | MVP PRD CAP-002 (MVP-F-005, MVP-F-006, MVP-F-007) | Proposed | Scheduler implementation approach not yet decided |
| Browser automation module | Headless browser interaction with BMAquiosque: login, navigate, check markings, register markings | MVP PRD CAP-001 (MVP-F-001, MVP-F-002, MVP-F-003) | Proposed | Browser automation library not yet decided (Playwright, Selenium, etc.) |
| Marking calculation engine | Calculate timing for entry, lunch-out, lunch-return, exit markings; enforce business rules; apply jitter | MVP PRD CAP-003 (MVP-F-008 through MVP-F-013) | Proposed | None — business rules are well-defined in PRD |
| Retry mechanism | Retry failed marking attempts up to 3 times with 5-minute intervals | MVP PRD CAP-001 (MVP-F-004) | Proposed | None |
| Configuration module | Load user configuration from environment variables or config file; validate inputs | MVP PRD MVP-F-016, MVP-VR-001 through MVP-VR-003 | Proposed | Config file format not yet decided (YAML, JSON, .env) |
| Logging module | File-based logging of all actions (markings, retries, errors, skips, scheduler runs) | MVP PRD CAP-007 (MVP-F-014, MVP-F-015) | Proposed | Logging library not yet decided |

### Full Product Additional Modules (Phase 2+)

| Area or Proposed Module | Expected Responsibility | Basis | Status | Open Questions |
| --- | --- | --- | --- | --- |
| Discord bot module | User registration, credential setup, configuration, pause/resume, status via Discord commands | Full PRD CAP-004 (F-014 through F-017) | Proposed (Phase 2) | Discord bot library not yet decided |
| Notification module | Discord notifications on successful markings and persistent failures | Full PRD CAP-005 (F-018, F-019) | Proposed (Phase 2) | Integration approach with Discord bot not yet decided |
| User management module | Individual user accounts, secure credential storage, schedule configuration, activation/deactivation | Full PRD CAP-006 (F-020 through F-023) | Proposed (Phase 2) | Database technology not yet decided |
| Holiday management module | Configurable holiday calendar to skip automation | Full PRD CAP-008 (F-026) | Proposed (Phase 3+) | None |
| Web dashboard module | Visual interface for monitoring markings and status | Full PRD CAP-009 (F-027) | Proposed (Phase 3+) | React confirmed as user constraint |
| API layer module | RESTful API for user and configuration management | Full PRD CAP-010 (F-028) | Proposed (Phase 3+) | API framework not yet decided |

### Proposed Java Project Layout

> **Note**: This layout is a conventional Java project proposal. The actual structure depends on technology decisions (Maven vs. Gradle, Spring Boot vs. Quarkus, etc.) that will be made during `technology-definition`.

```
auto-time-marking/
├── docs/                              # Documentation (exists)
│   ├── context/                       # Harness context documents
│   ├── product/                       # PRDs
│   ├── planning/                      # (Future) Project planning
│   ├── architecture/                  # (Future) Technology definition, ADRs
│   └── features/                      # (Future) Feature files, tech specs, tasks
├── src/
│   ├── main/
│   │   ├── java/                      # Java source code
│   │   │   └── <base.package>/
│   │   │       ├── application/       # Application entry point, bootstrap
│   │   │       ├── scheduler/         # Scheduler module
│   │   │       ├── automation/        # Browser automation module
│   │   │       ├── calculation/       # Marking calculation engine
│   │   │       ├── retry/             # Retry mechanism
│   │   │       ├── config/            # Configuration loading and validation
│   │   │       └── logging/           # Logging configuration
│   │   └── resources/                 # Configuration files, templates
│   └── test/
│       ├── java/                      # Unit and integration tests
│       └── resources/                 # Test configuration
├── build file (pom.xml or build.gradle) # Pending technology-definition
├── Dockerfile                         # (Future) Container deployment
└── README.md                          # (Future) Project documentation
```

Status: Proposed. All directories and files are conceptual. None exist in the codebase.

## Planned Deliverables

| Deliverable | Expected Purpose | Basis | Status | Notes |
| --- | --- | --- | --- | --- |
| Backend automation service (MVP) | Single-user time-clock automation on BMAquiosque | MVP PRD | Proposed | Java application with scheduler, browser automation, marking calculation, logging |
| Configuration file or env support | Single-user credentials and preferences | MVP PRD MVP-F-016 | Proposed | Format pending decision |
| Log files | Audit trail of all automated actions | MVP PRD CAP-007 | Proposed | File-based |
| Discord bot (Phase 2) | User interaction channel | Full PRD CAP-004 | Proposed (Phase 2) | Deferred from MVP |
| Web dashboard (Phase 3+) | Visual monitoring and configuration | Full PRD CAP-009 | Proposed (Phase 3+) | React confirmed as user constraint |
| REST API (Phase 3+) | Programmatic access and integration | Full PRD CAP-010 | Proposed (Phase 3+) | Deferred from MVP |

## Related Repositories

No related repositories identified.

| Repository | Location | Responsibility | Availability | Notes |
| --- | --- | --- | --- | --- |
| None | — | — | — | Self-contained project |

## Microservices

No microservices identified. The MVP is a single backend service.

| Service | Location | Responsibility | Entry Points | Notes |
| --- | --- | --- | --- | --- |
| None | — | — | — | Single-service architecture for MVP |

## Internal Libraries or Dependencies

No internal libraries or dependencies identified.

| Name | Location | Relationship | Evidence | Notes |
| --- | --- | --- | --- | --- |
| None | — | — | — | All dependencies will be external (third-party libraries) |

## External Integrations

| Integration | Evidence | Related Components | Notes |
| --- | --- | --- | --- |
| BMAquiosque | PRD (MVP-F-001, F-001) | Browser automation module | Critical external dependency. Web-based time-clock system. Exact URL, login flow, and page structure pending user input. |
| Discord | PRD (F-014 through F-019) | Discord bot module, Notification module | Phase 2+. User constraint (mandatory technology). |

## Cross-Repository Relationships

Not applicable. The project is self-contained with no cross-repository relationships.

## Technology Evidence

No codebase evidence available. No technologies were detected from code inspection.

| Area | Technology | Evidence | Status |
| --- | --- | --- | --- |
| Backend / Runtime | Java | User constraint (discovery interview) | User constraint — not confirmed by codebase |
| Frontend | React | User constraint (discovery interview) | User constraint — Phase 3+ dashboard |
| User interaction | Discord | User constraint (discovery interview, PRD) | User constraint — Phase 2+ bot |
| Browser automation | Headless browser library (e.g., Playwright, Selenium) | PRD requirement (CAP-001) | Pending confirmation — library not yet decided |
| Scheduler | Internal scheduler (implementation TBD) | PRD requirement (CAP-002) | Pending confirmation — approach not yet decided |
| Configuration format | YAML, JSON, or .env | PRD requirement (MVP-F-016) | Pending confirmation — format not yet decided |
| Build tool | Maven or Gradle | Java convention | Pending confirmation — not yet decided |
| Logging library | TBD | PRD requirement (CAP-007) | Insufficient evidence |
| Database | TBD | Required for multi-user (Phase 2+) | Insufficient evidence — not needed for MVP (file/config-based) |
| Credential encryption | TBD | PRD requirement (MVP-VR-003, F-021) | Insufficient evidence — approach not yet decided |

## Important Conventions Observed

| Convention | Evidence | Notes |
| --- | --- | --- |
| Harness workflow documents under `docs/` | `docs/context/`, `docs/product/` | Established by project discovery |
| PRDs organized by product name | `docs/product/auto-time-marking/` | Full Product PRD and MVP PRD confirmed |
| `.agents/` directory excluded from analysis | Discovery interview | Harness configuration, not part of the product |
| Documents written in English | PRDs and context documents | Established convention |

## Missing or Unclear Structure Information

- No implementation code exists to validate proposed module boundaries.
- No build tool or dependency management file exists to confirm Java project layout.
- No configuration file exists to confirm format or schema.
- No test framework or test structure has been established.
- Specific Java framework (Spring Boot, Quarkus, Micronaut, etc.) is not yet decided.
- Browser automation library is not yet decided.
- Database technology for Phase 2+ is not yet decided.
- Package naming convention is not yet established.
- CI/CD pipeline structure is not yet defined.
- Deployment strategy (container, bare JAR, etc.) is not yet defined.

## Open Structure Decisions

| Decision | Impact | Options or Notes | Owner or Next Step |
| --- | --- | --- | --- |
| Java framework | Determines project layout, dependency injection, configuration approach, scheduler implementation | Spring Boot, Quarkus, Micronaut, plain Java | Decide during `technology-definition` |
| Build tool | Determines build file, dependency management, project structure conventions | Maven (`pom.xml`) or Gradle (`build.gradle`) | Decide during `technology-definition` |
| Browser automation library | Determines browser interaction API, dependencies, test approach | Playwright for Java, Selenium, other | Decide during `technology-definition` |
| Configuration file format | Affects configuration module design and developer experience | YAML, JSON, .env, Spring properties | Decide during `technology-definition` |
| Package structure | Affects code organization within `src/main/java/` | By capability/module, by layer, hybrid | Decide during `technology-definition` or `tech-spec` |
| Logging library | Affects logging module implementation | SLF4J + Logback, Log4j2, JUL | Decide during `technology-definition` |
| Database (Phase 2+) | Affects user management module and credential storage | PostgreSQL, MySQL, H2, SQLite, other | Decide during Phase 2 `technology-definition` |
| Deployment strategy | Affects Dockerfile, build scripts, CI/CD | Container (Docker), executable JAR, systemd service | Decide during `technology-definition` |
| Multi-module vs single-module | Affects Maven/Gradle structure and build complexity | Single module for MVP; multi-module for Phase 2+ | Decide during `technology-definition` |

## Inputs for the Next Step

`project-analysis` should use the following inputs:

- **Primary**: `docs/context/project-structure.md` (this document)
- **Supporting**: `docs/context/project-discover.md`
- **Product context**: `docs/product/auto-time-marking/full-product-prd.md` and `docs/product/auto-time-marking/mvp-prd.md`

Since this is a Greenfield / No Codebase Available project, `project-analysis` must:

- Analyze requirements, conceptual structure, risks, expected modules, planned dependencies, and open decisions.
- NOT claim any implemented technical evidence.
- Focus on MVP scope (CAP-001, CAP-002, CAP-003, CAP-007) and the phased evolution path.
- Identify risks related to the BMAquiosque external dependency.
- Evaluate the proposed module structure against PRD requirements.

## Notes for `project-analysis`

- The project is greenfield. All structure is proposed/conceptual.
- The MVP has 4 capabilities, 16 features, and 5 user stories — all confirmed.
- The critical external dependency is BMAquiosque (URL, login flow, and page structure are still unknown).
- User-confirmed mandatory technologies: Java (backend), React (frontend, Phase 3+), Discord (interaction, Phase 2+).
- All other technology decisions are pending and should NOT be treated as confirmed.
- The proposed module structure follows capability boundaries from the PRD. Actual package/module design depends on technology decisions.
- Key risks to analyze: BMAquiosque UI stability, browser automation reliability, single-point-of-failure in single-user MVP, credential security, and the evolution path from single-user to multi-user.
- The architecture should support multi-user evolution (Phase 2) without requiring a full rewrite.
