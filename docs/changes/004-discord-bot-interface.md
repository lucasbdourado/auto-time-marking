# Change Spec: Discord Bot Interface (CAP-004)

## 1. Overview
This technical change specification defines the architecture, contract definitions, and interaction workflow for **CAP-004 (Discord Bot Interface)** in `auto-time-marking`. The Discord Bot serves as the primary, user-facing interaction channel for employees to self-register, securely configure BMAquiosque platform credentials, customize individual time-clock preferences (maximum entry time, lunch duration, jitter variation), pause or resume background automation, and query daily time-clock marking status.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Full Product PRD | [full-product-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/full-product-prd.md#L107) | Definitive requirements for CAP-004 (F-014 through F-017, US-004, UC-001 to UC-005) |
| Tech Stack Definition | [technology-definition.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/architecture/auto-time-marking/technology-definition.md#L120) | Identifies JDA (Java Discord API) as the selected Discord integration framework |
| Project Structure | [project-structure.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/context/project-structure.md#L155) | Architecture placement for Discord interaction module |
| Workflow Orchestrator | [MarkingWorkflowOrchestrator.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/workflow/service/MarkingWorkflowOrchestrator.java) | Execution engine that ingests per-user active status and schedule configurations |
| Existing Application Config | [BmaquiosqueProperties.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java) | Standard application properties pattern for module configuration |

## 3. Confirmed Facts vs Assumptions

### Confirmed Facts
- Discord is the mandatory, primary interaction interface for end users (employees).
- CAP-004 encompasses 4 core features:
  - **F-014**: Self-registration via Discord bot commands.
  - **F-015**: Secure BMAquiosque credential setup (ephemeral modal/DM to prevent credential leakage in public channels).
  - **F-016**: Schedule preference configuration (max entry time, lunch duration, jitter minutes).
  - **F-017**: Pause and resume automation toggle.
- Java Discord API (JDA 5.x) is the designated Java framework for Discord Slash Commands and Discord interactions.
- Commands must leverage native Discord Slash Commands (`/register`, `/credentials`, `/config`, `/pause`, `/resume`, `/status`).

### Assumptions & Open Questions
- Discord Bot Token will be supplied via `DISCORD_BOT_TOKEN` environment variable or `discord.bot.token` configuration property.
- When `discord.bot.enabled=false` (e.g. during standalone headless testing or unit tests), the Discord bot bean initialization is skipped gracefully.
- User profiles mapped via Discord User ID connect directly with the multi-user domain (CAP-006).

## 4. Current vs Expected Behavior

### Current Behavior
- Configuration (credentials, max entry time, jitter) is statically bound via `application.properties` or environment variables for a single default profile.
- There is no interactive interface or bot listener registered in the application.

### Expected Behavior
- Spring Boot initializes a JDA Discord Bot listener on startup if `discord.bot.enabled=true`.
- Discord Slash Commands are registered globally or per guild:
  - `/register`: Creates user account linked to Discord User ID.
  - `/credentials`: Securely prompts for BMAquiosque username & password via ephemeral form/modal or DM.
  - `/config`: Updates max entry time (e.g., `09:00`), jitter (e.g., `5`), and lunch preferences.
  - `/pause`: Suspends automated background marking for the user.
  - `/resume`: Re-activates background marking for the user.
  - `/status`: Displays current user configuration, active state, and today's marking summary.
- Sensitive responses (e.g. credential confirmation or errors) are returned strictly as **Ephemeral Messages** (`setEphemeral(true)`).

## 5. Scope & Out of Scope

### In Scope
- JDA 5.x dependency integration in `pom.xml`.
- Creation of `modules/interaction/discord` package following DDD / Clean Architecture.
- `DiscordBotProperties` configuration binding (`discord.bot.token`, `discord.bot.enabled`, `discord.bot.guild-id`).
- `DiscordBotService` lifecycle manager (start/stop JDA instance).
- JDA `ListenerAdapter` implementing Slash Command handlers for `/register`, `/credentials`, `/config`, `/pause`, `/resume`, `/status`.
- Ephemeral security enforcement for sensitive data handling.
- Unit and integration tests for command handlers with mocked JDA events.

### Out of Scope
- Web Dashboard UI (Phase 3).
- Direct REST API endpoint creation for external third-party webhooks (CAP-010).

## 6. Functional Acceptance Criteria

### AC-001: Bot Lifecycle & Conditional Startup
**Given** application configured with `discord.bot.enabled=true` and valid `discord.bot.token`  
**When** Spring Boot context initializes  
**Then** `DiscordBotInitializer` establishes WebSocket connection with Discord Gateway and registers slash commands.

### AC-002: User Self-Registration (`/register`)
**Given** a Discord user executing `/register`  
**When** the command is processed  
**Then** the system registers a new user profile bound to their Discord User ID and returns an ephemeral confirmation message instructing them to set credentials via `/credentials`.

### AC-003: Secure Credential Submission (`/credentials`)
**Given** a registered user executing `/credentials`  
**When** supplying BMAquiosque username and password  
**Then** the command response MUST be marked as ephemeral (`setEphemeral(true)`), credentials are encrypted and stored in user storage, and password is never logged or exposed in public chat channels.

### AC-004: Preference Configuration (`/config`)
**Given** a registered user executing `/config max_entry_time:09:00 jitter:5`  
**When** command parameters are validated  
**Then** the user's schedule configuration is updated, and an ephemeral confirmation summarizing new settings is returned.

### AC-005: Pause and Resume Controls (`/pause` & `/resume`)
**Given** an active user executing `/pause`  
**When** processed  
**Then** automation status is toggled to `PAUSED`, preventing background marking execution for this user until `/resume` is called.

### AC-006: Status Query (`/status`)
**Given** a registered user executing `/status`  
**When** processed  
**Then** an ephemeral Discord Embed is returned detailing user activation state (`ACTIVE` or `PAUSED`), current settings, and today's markings retrieved from SSOT.

## 7. Technical Design & Contracts

### Module Architecture (`modules/interaction/discord`)
```
com.lucasbdourado.autotimemarking.modules.interaction.discord/
├── domain/
│   ├── model/
│   │   └── DiscordUserCommand.java
│   └── port/
│       └── UserInteractionPort.java
├── service/
│   └── DiscordCommandHandlerService.java
└── infrastructure/
    ├── config/
    │   └── DiscordBotProperties.java
    └── jda/
        ├── DiscordBotInitializer.java
        ├── DiscordSlashCommandListener.java
        └── DiscordEmbedFactory.java
```

### Slash Command Specifications
| Command | Parameters | Visibility | Description |
| --- | --- | --- | --- |
| `/register` | None | Ephemeral | Registers new user account linked to Discord User ID |
| `/credentials` | `username` (string), `password` (string) | Ephemeral | Sets BMAquiosque credentials securely |
| `/config` | `max_entry` (string, e.g. "09:00"), `jitter` (int, e.g. 5) | Ephemeral | Configures max entry time and jitter variation |
| `/pause` | None | Ephemeral | Pauses automated time-clock marking |
| `/resume` | None | Ephemeral | Resumes automated time-clock marking |
| `/status` | None | Ephemeral | Displays active status, settings, and today's markings |

## 8. Sequential Implementation Checklist

- [x] **Task 1: Discord Bot Dependency & Properties Configuration**
  - **Goal**: Add JDA 5.x dependency to `pom.xml` and create `DiscordBotProperties` with validation.
  - **Acceptance**: Application builds cleanly with `discord.bot.*` properties configurable via `application.properties`.
  - **Dependencies**: None.

- [x] **Task 2: Discord Bot Initialization Engine**
  - **Goal**: Create `DiscordBotInitializer` component to lifecycle-manage JDA client connection and Slash Command registration.
  - **Acceptance**: JDA connects when enabled and skips initialization cleanly when `discord.bot.enabled=false`.
  - **Dependencies**: Task 1.

- [x] **Task 3: Slash Command Event Listener & Security Handler**
  - **Goal**: Implement `DiscordSlashCommandListener` handling command routing and enforcing `setEphemeral(true)` for all responses.
  - **Acceptance**: Slash commands dispatch to proper handler services with strict privacy guarantees.
  - **Dependencies**: Task 2.

- [x] **Task 4: Command Handler Services Implementation**
  - **Goal**: Implement business logic for `/register`, `/credentials`, `/config`, `/pause`, `/resume`, and `/status`.
  - **Acceptance**: User commands manipulate user profile state, credentials, and configuration correctly.
  - **Dependencies**: Task 3.

- [x] **Task 5: Comprehensive Unit & Integration Testing**
  - **Goal**: Create unit and integration test suite covering Discord command routing, credential security, and status queries using Mockito.
  - **Acceptance**: All tests pass cleanly (`mvn clean test` with `BUILD SUCCESS`).
  - **Dependencies**: Task 4.
