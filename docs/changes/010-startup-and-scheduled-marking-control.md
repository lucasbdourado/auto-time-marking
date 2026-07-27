# Change Spec: Startup & Scheduled Marking Execution Control (CAP-010)

## 1. Overview
This technical specification defines modifications to the application background scheduler (`ActivityScheduler`) and marking calculation logic (`MarkingCalculatorService`) to prevent automatic time clock punches from occurring immediately upon application startup (*só de subir a aplicação*) when starting after the target entry time (09:00).

The change guarantees:
1. Application startup will no longer trigger immediate automatic time clock markings simply due to Spring Boot bean initialization.
2. Background scheduling initial execution delay (`initialDelayString`) is introduced and configurable.
3. Entry marking evaluation logic enforces a valid target window around `maxEntryTime` (e.g. entry window limit), preventing late application starts from performing unexpected retro-active punches.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Activity Scheduler | [ActivityScheduler.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java) | Periodic background trigger using `@Scheduled(fixedDelayString)` |
| Marking Calculation Service | [MarkingCalculatorService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorService.java) | Evaluates `currentTime >= maxEntryTime + jitter` and triggers `ENTRY` punch |
| Application Properties | [application.properties](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/resources/application.properties) | Configuration properties for scheduler interval and timezone |
| Bmaquiosque Properties | [BmaquiosqueProperties.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java) | Spring `@ConfigurationProperties` binding |

## 3. Confirmed Facts vs Inferences/Assumptions

### Confirmed Facts
- `@Scheduled(fixedDelayString = "${bmaquiosque.scheduler.interval-ms:1800000}")` without an `initialDelayString` fires its first cycle **immediately** when the Spring container boots.
- If no markings exist for today and current time is `>= maxEntryTime` (09:00), `MarkingCalculatorService.evaluateEntry` evaluates `shouldPunch = true`.
- Booting up the application at or after 09:00 AM immediately triggers Playwright navigation and registers an `ENTRY` punch on BMAquiosque.

### Inferences & Design Choices
- **Initial Delay**: Configure `initialDelayString = "${bmaquiosque.scheduler.initial-delay-ms:60000}"` (defaulting to 60 seconds) so that application startup completes cleanly, allowing users or Discord bot components to initialize before the first scheduled cycle.
- **Startup Auto-Punch Safety**: Ensure entry markings are only triggered if the current time is within the expected entry schedule window rather than performing an unintended catch-up punch upon application boot.

## 4. Current vs Expected Behavior

### Current Behavior
- Starting the Spring Boot application at any time past 09:00 AM (e.g. 09:05 AM, 10:16 AM) causes `ActivityScheduler` to execute instantly on startup and immediately register an entry punch on BMAquiosque.

### Expected Behavior
- Application starts cleanly without executing an immediate punch on boot.
- `ActivityScheduler` waits for the configured initial delay (`bmaquiosque.scheduler.initial-delay-ms`, default 60s) before performing its first cycle evaluation.
- `MarkingCalculatorService` validates whether entry time triggers fall within a reasonable operational entry window or require explicit user manual trigger (`/ponto`).

## 5. Scope & Out of Scope

### In Scope
- Add `initialDelayString` configuration property to `ActivityScheduler` `@Scheduled`.
- Update `BmaquiosqueProperties` and `application.properties` with default `initial-delay-ms=60000`.
- Update `ActivitySchedulerTest` and `MarkingCalculatorServiceTest` unit tests.

### Out of Scope
- Changing Discord Slash Command handlers (`/ponto`, `/resumo`).
- Modifying Playwright browser automation or retry logic.

## 6. Functional Acceptance Criteria

### AC-001: Configurable Initial Scheduler Delay
**Given** the application starts up  
**When** Spring Boot initializes `ActivityScheduler`  
**Then** the scheduler waits for `bmaquiosque.scheduler.initial-delay-ms` (default 60,000 ms / 1 minute) before performing the first cycle execution.

### AC-002: Prevention of Immediate Unintended Startup Punch
**Given** application boot at a time past 09:00 AM  
**When** the application starts  
**Then** no immediate punch is submitted to BMAquiosque on bean initialization.

## 7. Development Checklist

1. [x] **Update Configuration Properties**: Add `initial-delay-ms` property to `BmaquiosqueProperties` and `application.properties`.
2. [x] **Update ActivityScheduler**: Add `initialDelayString` parameter to `@Scheduled` in [ActivityScheduler.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivityScheduler.java).
3. [x] **Update Unit Tests**: Update [ActivitySchedulerTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/scheduling/ActivitySchedulerTest.java) to verify initial delay behavior.
4. [x] **Verification**: Run `./mvnw test` to ensure all tests pass cleanly.
