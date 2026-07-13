# Auto Time Marking

Auto Time Marking is a backend automation service designed to automatically register daily workday time-clock entries (ponto) on the BMAquiosque platform. It aims to eliminate forgotten markings and ensure consistent work hour records (8h45 effective workday) for employees with flexible hours.

## Technologies

- **Language & Runtime**: Java 21 (LTS)
- **Framework**: Spring Boot 3.4.x
- **Build Tool**: Maven
- **Browser Automation**: Playwright for Java
- **Configuration**: application.properties + System Environment Variables
- **Logging**: SLF4J + Logback (Rolling File Appender)
- **Testing**: JUnit 5 + Mockito + Spring Boot Test

---

## Implementation Order

Following the Harness workflow and the project planning, the implementation is structured into two main delivery phases to ensure stability and incremental validation:

### Phase 1: Foundation and Configuration
Focuses on setting up the application chassis, property loading, logging configuration, and background scheduling loop.
1. **`single-user-configuration`**: Load and validate credentials and user parameters from `application.properties` or environment variables at boot time.
2. **`audit-logging`**: Set up SLF4J and Logback rolling file appenders to log system actions, skips, and errors before executing any automated actions.
3. **`activity-scheduler`**: Implement a Spring task scheduling loop running every 30 minutes, restricted to the operating window (05:00 - 22:00, Monday to Friday).

### Phase 2: Marking Automation and Rules
Focuses on headless browser automation, business calculation logic, and automated punch submissions.
4. **`bmaquiosque-automation`**: Setup Playwright headless browser instance to log in, navigate, check registered markings, click the register punch button, and handle retries (up to 3 times, 5-minute intervals).
5. **`marking-calculation`**: Core business rules engine. Computes when pending markings must be battue, enforcing 8h45 work logic, variable lunch durations (1h to 2h), max 6h work limit, and applying natural random time variation (jitter).

---

## Harness Documentation

This project uses the **Harness Context Engineering** workflow. All context, planning, and architectural documents can be found under the `docs/` directory:

- **Context Documents**:
  - Discovery Context: [project-discover.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-discover.md)
  - Project Structure: [project-structure.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-structure.md)
  - Project Analysis: [project-analysis.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-analysis.md)
- **Product Requirements**:
  - Full Vision PRD: [full-product-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/full-product-prd.md)
  - MVP Scope PRD: [mvp-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/mvp-prd.md)
- **Project Planning**:
  - MVP Planning: [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/planning/auto-time-marking/project-planning.md)
- **Architectural Decisions**:
  - Technology Definition: [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md)
