# Change Spec: Discord Workday Markings Summary Command (CAP-008)

## 1. Overview
This technical specification defines the implementation of a Discord slash command (`/ponto` / `/resumo`) and enhanced workday status display that fetches live time clock markings from BMAquiosque and presents a rich `MessageEmbed` daily summary to the user.

The summary format matches the design pattern established in `reminderbot` (`buildWorkDaySummaryEmbed`), displaying today's date, registered markings list (Entrada, Saída Almoço, Retorno Almoço, Saída), worked time, remaining time to complete the daily shift (8h45 / 525 minutes), and estimated exit time.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Reference Summary Embed | `C:\Users\lucas.dourado\IdeaProjects\reminderbot\src\main\java\br\com\reminderbot\service\DiscordPrivateMessageService.java` | Standard layout and formatting for workday summary embeds |
| Marking Calculator | [MarkingCalculatorService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorService.java) | Calculates target times and workday states |
| Time Clock Domain Port | [TimeClockClient.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/automation/domain/TimeClockClient.java) | Retrieves daily markings from BMAquiosque |
| Slash Command Listener | [DiscordSlashCommandListener.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/infrastructure/jda/DiscordSlashCommandListener.java) | Listens to Discord slash interaction events and sends responses |
| Bot Initializer | [DiscordBotInitializer.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/infrastructure/jda/DiscordBotInitializer.java) | Registers slash commands with Discord gateway |

## 3. Confirmed Facts vs Assumptions

### Confirmed Facts
- BMAquiosque daily markings are retrieved via `TimeClockClient.retrieveDailyMarkings(username, password)` as a `List<LocalTime>`.
- Standard daily work shift is 525 minutes (8 hours and 45 minutes).
- Discord slash commands time out after 3 seconds if not deferred; Playwright web interactions require asynchronous deferred replies (`event.deferReply(true)`).
- All user interaction text, embed titles, and field names must be in **Portuguese**.

### Assumptions & Design Choices
- **Command Names**: `/ponto` and `/resumo` will be registered as slash commands to fetch and display the workday summary embed. `/status` will remain for automation configuration status and can also link to `/ponto`.
- **Deferred Response**: `DiscordSlashCommandListener` will execute `event.deferReply(true).queue()` before calling `retrieveDailyMarkings` to avoid Discord interaction timeouts during Playwright page navigation.
- **Summary Model**: Create `WorkdaySummary` record in `modules.calculation.domain` and `WorkdaySummaryService` in `modules.calculation.service` to calculate worked time, remaining time, and exit time independently of Discord UI code.

## 4. Current vs Expected Behavior

### Current Behavior
- `/status` displays user configuration (active status, BMA username, max entry time, jitter) as plain text.
- There is no command to query current daily markings or view worked/remaining hours.

### Expected Behavior
- Slash commands `/ponto` and `/resumo` fetch today's markings live from BMAquiosque.
- The bot replies with a green `MessageEmbed` formatted as follows:
  - **Title**: `Resumo do ponto - dd/MM/yyyy`
  - **Data**: `dd/MM/yyyy`
  - **Marcações**: Formatted list of times (e.g., `Entrada: 08:00\nSaída Almoço: 12:00\nRetorno Almoço: 13:00`) or `Nenhuma marcação encontrada`.
  - **Tempo trabalhado**: `HH:mm` (e.g., `04:00` or `08:45`).
  - **Tempo restante**: `HH:mm` (e.g., `04:45` or `00:00`).
  - **Horário de saída**: `HH:mm` (estimated exit time calculated based on actual entry and lunch interval).

## 5. Scope & Out of Scope

### In Scope
- Creating `WorkdaySummary` domain record and `WorkdaySummaryService` to calculate shift metrics (worked minutes, remaining minutes, estimated exit time, formatted markings).
- Creating `DiscordWorkdayEmbedBuilder` utility to generate `MessageEmbed`.
- Registering `/ponto` and `/resumo` slash commands in `DiscordBotInitializer`.
- Updating `DiscordSlashCommandListener` to defer slash command interaction and reply with the summary embed.
- Adding comprehensive unit tests for `WorkdaySummaryService` and embed construction.

### Out of Scope
- Interactive buttons for manual clock-in directly from the summary embed (automation remains scheduled/automated).

## 6. Functional Acceptance Criteria

### AC-001: Live Daily Summary Embed Generation
**Given** a registered user with valid BMAquiosque credentials  
**When** the user executes `/ponto` or `/resumo` in Discord  
**Then** the bot defers the reply, retrieves today's markings from BMAquiosque, and replies with a `MessageEmbed` titled `Resumo do ponto - <data>` containing Data, Marcações, Tempo trabalhado, Tempo restante, and Horário de saída.

### AC-002: Worked & Remaining Time Calculation
**Given** 1, 2, 3, or 4 registered markings for today  
**When** the summary is calculated  
**Then** `Tempo trabalhado` accurately sums elapsed shift intervals, `Tempo restante` subtracts worked time from 525 minutes (8h45), and `Horário de saída` projects the exit time.

### AC-003: Empty Markings Handling
**Given** no registered markings for today  
**When** the user executes `/ponto`  
**Then** `Marcações` displays `Nenhuma marcação encontrada`, `Tempo trabalhado` displays `00:00`, `Tempo restante` displays `08:45`, and `Horário de saída` displays `Não calculado`.

### AC-004: Missing Credentials Error Handling
**Given** a Discord user who has not set BMAquiosque credentials  
**When** the user executes `/ponto`  
**Then** the bot replies with an ephemeral message asking the user to run `/credentials` first.

## 7. Technical Design & Implementation Checklist

### 7.1 Sequential Implementation Checklist
1. **WorkdaySummary Domain Model**: Create `WorkdaySummary` record (`date`, `markings`, `workedMinutes`, `remainingMinutes`, `estimatedExitTime`, `status`).
2. **WorkdaySummaryService**: Implement shift metric calculation (8h45 total work requirement, lunch duration handling, formatted output).
3. **DiscordWorkdayEmbedBuilder**: Implement `MessageEmbed` builder producing GREEN embeds with exact field structure matching reference code.
4. **DiscordBotInitializer Update**: Add `/ponto` and `/resumo` slash command definitions to `registerCommands()`.
5. **DiscordSlashCommandListener Update**: Add command handling using `event.deferReply(true)` and asynchronous completion hook.
6. **Unit Tests**: Add tests for `WorkdaySummaryServiceTest` and `DiscordWorkdayEmbedBuilderTest`.
7. **Verification**: Run `mvn clean test` to verify zero regressions.
