# Technology Definition

## Status

Status: Confirmed

Technology definition readiness: Ready for Tech Spec

Last updated: 2026-07-13

Owner or primary stakeholder: Lucas Dourado

## Product Name

Auto Time Marking

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Project Discovery | [project-discover.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-discover.md) | Discovery | Confirmed | Defines greenfield context & constraints |
| Project Structure | [project-structure.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-structure.md) | Structure | Confirmed | Defines proposed project layout |
| Project Analysis | [project-analysis.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-analysis.md) | Analysis | Confirmed | Evaluates requirements & risks |
| Project Planning | [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/planning/auto-time-marking/project-planning.md) | Planning | Confirmed | Defines MVP features and order |
| Full Product PRD | [full-product-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/full-product-prd.md) | Full Product PRD | Confirmed | Approved |
| MVP PRD | [mvp-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/mvp-prd.md) | MVP PRD | Confirmed | Approved |

## Definition Scope

This definition is a project-level technology definition for the Auto Time Marking MVP and its phased evolution.

Target output path: `docs/architecture/auto-time-marking/technology-definition.md`

Technology reference path pattern: `docs/references/auto-time-marking/technologies/<technology-name>.md`

## Planning Summary

The MVP (Phase 1) is focused on automated time-clock registration on the BMAquiosque platform for a single user, run in the background on a server with no UI, no database, and no Discord bot.
- **Phase 1: Foundation and Configuration**: Establish project framework, configuration loading, logging, and scheduling loop.
- **Phase 2: Marking Automation and Rules**: Implement browser automation, business rule calculations, retries, and file auditing.
- **Downstream steps blocked by decisions**: Technical specification (`tech-spec`), task breakdown (`create-tasks`), and implementation planning (`plan-task`).
- **Risks**: UI selector stability on BMAquiosque, headless browser stability, plain-text configuration of credentials.

## Project Scenario

Scenario: Greenfield / No Codebase Available

Codebase inspected: No

If no codebase was inspected, explain why: The project is greenfield with no implementation code yet. The workspace contains only Harness `.agents` configuration, `.git`, and `docs/`.

## Technology Decision Principles

```text
The agent recommends.
The user decides.
```

- Technologies detected from clear codebase evidence are existing confirmed decisions.
- Technologies explicitly required by the user are confirmed decisions or user constraints.
- Technologies inferred from PRD, planning, feature files, or requirements are pending recommendations until confirmed.
- Pending, rejected, or deferred technologies must not be treated as confirmed.
- Context7 documentation is captured only for confirmed or explicitly selected technologies.
- Unclear technology needs remain open questions.

## Feature Technology Needs

| Feature | Technology Need | Reason | Priority | Status |
| --- | --- | --- | --- | --- |
| `single-user-configuration` | Config property parsing and loading | Load credentials and parameters from external source at boot | Must | Confirmed (Spring Boot Properties) |
| `activity-scheduler` | Scheduling loop mechanism | Execute marking checks every 30 minutes in Mon-Fri 05:00-22:00 window | Must | Confirmed (Spring Scheduler) |
| `bmaquiosque-automation` | Browser automation framework | Log in, check markings, and register punches on BMAquiosque | Must | Confirmed (Playwright for Java) |
| `marking-calculation` | Core calculation logic / Unit tests | Decide when to submit punches, apply jitter, recalculate exit | Must | Confirmed (Plain Java + JUnit 5) |
| `audit-logging` | Logging framework / Rotation | Record system actions to rotating file logs | Should | Confirmed (Logback + SLF4J) |

## Required Technology Decisions

| Area | Decision Needed | Source | Blocks Next Step? | Notes |
| --- | --- | --- | --- | --- |
| Backend Runtime | Java Version | Discovery constraint | Yes, all next steps | User constraint. Java 21 (LTS) chosen. |
| Build Tool | Build and Dependency management | Project Structure / Java convention | Yes, all next steps | Maven (pom.xml) chosen. |
| Framework | Application runtime core | Discovery framework question | Yes, all next steps | Spring Boot chosen. |
| Browser Automation | Automation library | Discovery library question | Yes, before Tech Spec | Playwright for Java chosen. |
| Configuration | Config format | Discovery format question | Yes, before Tech Spec | properties (`application.properties`) chosen. |
| Scheduling | Scheduling engine | MVP scheduler feature | Yes, before Tech Spec | Spring Tasks scheduling chosen. |
| Logging | Logging configuration | MVP logging feature | Yes, before Tech Spec | SLF4J + Logback chosen. |
| Testing | Test library | Java convention | Yes, before Tech Spec | JUnit 5 + Spring Boot Test + Mockito chosen. |

## Confirmed Technology Decisions

| Area | Technology | Decision Source | Evidence or Rationale | Documentation Reference | Notes |
| --- | --- | --- | --- | --- | --- |
| Language & Runtime | Java 21 (LTS) | User constraint | Mandatory user requirement. Java 21 brings virtual threads, record templates, and modern language features. | Not applicable | Set as system constraint. |
| Build Tool | Maven | Confirmed by user | Predictable build tool with deep IDE support. | Not applicable | Uses `pom.xml`. |
| Backend Framework | Spring Boot 3.4.x | Confirmed by user | Simplifies background task execution, property injection, scheduling, logging, and offers easy upgrade path to REST APIs (Phase 3+) and Discord Bot integrations (Phase 2+). | [springboot.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/springboot.md) | Standard dependency injection and lifecycle hooks. |
| Browser Automation | Playwright for Java | Confirmed by user | Fast, modern, auto-waiting, runs headless browser out-of-the-box, doesn't require separate driver binaries. | [playwright.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/playwright.md) | Eliminates flaky Selenium waits. |
| Configuration | application.properties | Confirmed by user | Natively supported by Spring Boot. Allows loading properties and interpolating environment variables for credential security. | [springboot.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/springboot.md) | Standard properties file in resources. |
| Scheduling | Spring Scheduling | Confirmed by user (as part of Spring Boot) | `@Scheduled` annotation is easy, flexible, and manages threads via `ThreadPoolTaskScheduler` natively. | [springboot.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/springboot.md) | Enabled with `@EnableScheduling`. |
| Logging | SLF4J + Logback | Confirmed by user (as part of Spring Boot) | Default logging system for Spring Boot. Supports Console and Rolling File appenders natively. | [springboot.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/springboot.md) | Configured in `logback-spring.xml`. |
| Testing | JUnit 5 + Spring Boot Test + Mockito | Confirmed by user (as part of Spring Boot) | Standard Spring Boot testing libraries. Mockito is ideal for testing calculations without calling Playwright. | [springboot.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/springboot.md) | JUnit 5 for unit/integration testing. |

## Existing Technologies Detected in Codebase

No codebase technologies were detected since the project is currently greenfield with no codebase implementation.

## Pending Technology Recommendations

No pending recommendations. All core technology choices for the MVP have been explicitly selected and confirmed by the user.

## Rejected or Deferred Technologies

| Area | Technology | Reason | Status |
| --- | --- | --- | --- |
| Configuration format | YAML / application.yml | User preferred properties format (`application.properties`) | Rejected |
| Configuration format | .env file | User preferred properties format (`application.properties`) | Rejected |
| Browser Automation | Selenium WebDriver | User preferred Playwright for Java; Selenium is harder to run headless and requires external webdrivers. | Rejected |
| Backend Framework | Quarkus / Micronaut | User preferred Spring Boot for ease of future Discord and API integration. | Deferred |
| Backend Framework | Plain Java (No Framework) | User preferred Spring Boot to facilitate scheduling, configuration, and future evolution. | Rejected |
| Database | PostgreSQL / MySQL / H2 | Not needed for single-user MVP. Will be required for Phase 2+ (multi-user). | Deferred |
| Discord Integration | JDA (Java Discord API) | Discord Bot features are deferred to Phase 2. | Deferred |
| Frontend | React | React is a user constraint for the Web Dashboard, but dashboard is deferred to Phase 3+. | Deferred |

## Technology Documentation References

| Technology | Reference File | Source | Status |
| --- | --- | --- | --- |
| Spring Boot | [springboot.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/springboot.md) | Context7 | Captured |
| Playwright for Java | [playwright.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/references/auto-time-marking/technologies/playwright.md) | Context7 | Captured |

## Internal Technology Guidelines

List of internal coding guidelines, Clean Architecture, or best practice reference documents relevant to the confirmed technologies.

| Area | Guideline | Path | Applies When |
|---|---|---|---|
| Java / Clean Architecture | Java/Clean Architecture Guidelines | [.agents/docs/architecture/coding-guidelines/README.md](file:///.agents/docs/architecture/coding-guidelines/README.md) | Used during tech spec, task design, and implementation of all Java modules. |
| React Frontend | React Coding Guidelines | [.agents/docs/architecture/react-coding-guidelines/README.md](file:///.agents/docs/architecture/react-coding-guidelines/README.md) | (Future) Will apply in Phase 3+ when developing the Web Dashboard. |

## Context7 Documentation Capture

| Technology | Context7 Library ID | Query Focus | Capture Status | Notes |
| --- | --- | --- | --- | --- |
| Spring Boot | `/spring-projects/spring-boot/v3.4.1` | Scheduling, task pool size customization, external configurations. | Captured | Resolved successfully. |
| Playwright for Java | `/microsoft/playwright-java` | Headless setup, try-with-resources, page navigation, clicking selectors. | Captured | Resolved successfully. |

## ADR Candidates

| Candidate ADR | Decision Area | Technology | Reason | Status |
| --- | --- | --- | --- | --- |
| ADR-001 | Java Framework Selection | Spring Boot | Core framework choice for lifecycle, DI, scheduling, logging, and future expansion. | Ready for ADR |
| ADR-002 | Browser Automation Engine | Playwright for Java | Choice of headless web scraping and browser automation library. | Ready for ADR |
| ADR-003 | Configuration Strategy | Properties & Env Vars | How configuration parameters and sensitive credentials are loaded. | Ready for ADR |

## Open Questions

- What is the exact URL and login flow of BMAquiosque? (Blocks browser automation implementation, to be provided by user).
- What is the exact DOM structure and selectors for markings on BMAquiosque? (Blocks browser automation selectors, to be discovered/provided).
- What is the target server operating system/hosting environment? (Required to verify browser packages installation, e.g., Chromium headless dependencies on Linux).

## Risks and Constraints

| Risk or Constraint | Area | Impact | Mitigation or Follow-Up | Status |
| --- | --- | --- | --- | --- |
| User Constraint | Java 21 | High | Enforced in `pom.xml` build configurations. | Confirmed Constraint |
| User Constraint | React Frontend | Low (MVP) | Deferred to Phase 3+; does not affect MVP implementation. | Confirmed Constraint |
| User Constraint | Discord Interaction | Low (MVP) | Deferred to Phase 2; MVP will use local configuration/logs. | Confirmed Constraint |
| BMAquiosque UI selector changes | Browser Automation | High | Isolate all selectors and browser actions inside Page Objects or Adapters. Log screenshots on failure. | Open Risk |
| Plain-text credentials exposure | Security | Medium | Instruct the user to pass sensitive credentials via system environment variables mapped inside `application.properties` (e.g. `bmaquiosque.password=${BMAQUIOSQUE_PASSWORD}`). | Mitigated |
| Server headless browser dependencies | Runtime | Medium | Ensure target server environment can run Chromium via Playwright (or suggest using a Playwright-friendly base Docker image). | Open Risk |

## Inputs for Tech Spec

The feature Tech Spec must be designed with the following confirmed inputs:
- **Language**: Java 21.
- **Build**: Maven.
- **Framework**: Spring Boot 3.4.x.
- **Automation**: Playwright for Java.
- **Properties**: `application.properties` binding config parameters and environment variables.
- **Scheduling**: `@Scheduled` thread pool configured to size 1 (single-user background automation doesn't require concurrent scheduler runs).
- **Guidelines**: Clean Architecture guidelines in [.agents/docs/architecture/coding-guidelines](file:///.agents/docs/architecture/coding-guidelines/README.md).

## Inputs for Create Tasks

Task creation should define steps for:
1. Creating the Maven `pom.xml` with Spring Boot 3.4.1 starter dependencies and Playwright Java library.
2. Creating the `application.properties` with placeholder properties and environment variable injection for credentials.
3. Setting up Logback rolling file configuration.
4. Implementing the configuration parser and validator, failing startup on invalid rules.
5. Setting up `@EnableScheduling` and the background scheduler cycle.
6. Creating the Playwright browser automation client.
7. Implementing the business logic for workday calculations and jitter, fully covered by JUnit 5 tests.

## Next Recommended Steps

- Ask user for approval of this Technology Definition and the associated technology reference files.
- Once approved, save the main technology definition at `docs/architecture/auto-time-marking/technology-definition.md` and reference files under `docs/references/auto-time-marking/technologies/`.
- Proceed to the **Technical Specification** (`tech-spec`) phase for the features in Phase 1 (Configuration, Audit Logging, and Activity Scheduler).
