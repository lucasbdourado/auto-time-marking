# Change Spec: Markings Display & Workday Shift Limits (CAP-009)

## 1. Overview
This technical specification defines modifications to daily time markings formatting and workday shift limit calculations displayed in the Discord summary embed (`/ponto` and `/resumo`). 

The change ensures:
1. Only actually registered markings are displayed in the list of markings (no placeholder labels or unused index slots like `Marcação 5`).
2. Maximum shift limits are calculated and displayed:
   - Maximum lunch break duration is 2 hours (120 minutes).
   - Maximum exit time ("Horário máximo de saída") is at most 6 hours after returning from lunch (e.g., if lunch return is 13:00, maximum exit time is 19:00).
   - Estimated exit time ("Horário de saída") is accurately calculated as soon as entry and lunch interval are defined.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Workday Summary Service | [WorkdaySummaryService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/WorkdaySummaryService.java) | Calculates worked time, remaining time, estimated exit, and formats markings |
| Workday Summary Model | [WorkdaySummary.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/domain/WorkdaySummary.java) | Domain record carrying workday summary state |
| Discord Embed Builder | [DiscordWorkdayEmbedBuilder.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/service/DiscordWorkdayEmbedBuilder.java) | Constructs Discord `MessageEmbed` for workday status |
| Marking Calculator | [MarkingCalculatorService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/calculation/service/MarkingCalculatorService.java) | Business rules for max continuous work (6h) and minimum lunch (1h) |

## 3. Confirmed Facts vs Inferences/Assumptions

### Confirmed Facts
- Standard daily work shift is 8h45 (525 minutes).
- Maximum continuous work block allowed before taking lunch or after returning from lunch is 6 hours (360 minutes).
- Maximum allowed lunch break duration is 2 hours (120 minutes).
- Markings list must display only registered punches (1 to 4 max), matching exact registered order.

### Inferences & Design Choices
- **Maximum Exit Time**: When 3 markings (Entry, Lunch Out, Lunch Return) or 4 markings are present, `maxExitTime` is `lunchReturn + 6 hours`.
- **Maximum Lunch Return Time**: When Lunch Out is registered (2 markings), `maxLunchReturnTime` is `lunchOut + 2 hours`.
- **Estimated Exit Time**:
  - 1 marking (Entry): estimated exit assumes default 1h lunch (Entry + 1h + 8h45).
  - 2 markings (Entry, Lunch Out): estimated exit assumes default 1h lunch return from Lunch Out time (Lunch Out + 1h + remaining minutes).
  - 3 markings (Entry, Lunch Out, Lunch Return): estimated exit is exact (Entry + Lunch Duration + 8h45).
  - 4 markings: final exit time recorded.

## 4. Current vs Expected Behavior

### Current Behavior
- `formatMarkings` prints labels by index. If there are 5 markings, it prints `Marcação 5: HH:mm`.
- Embed shows only standard `Horário de saída` without indicating `Horário máximo de saída` (6h post-lunch limit) or `Tempo máximo de almoço` (2h limit).

### Expected Behavior
- `formatMarkings` prints ONLY the actual registered markings, up to 4 standard labels (`Entrada`, `Saída Almoço`, `Retorno Almoço`, `Saída`).
- `WorkdaySummary` record enhanced with fields:
  - `maxExitTime` (`LocalTime`): 6 hours after `lunchReturn` (or after estimated max lunch return).
  - `maxLunchReturnTime` (`LocalTime`): 2 hours after `lunchOut`.
- Discord embed includes fields:
  - `Horário de saída`: estimated exit time based on entry and lunch interval.
  - `Horário máximo de saída`: max 6h after lunch return (e.g. `19:00` if lunch return is `13:00`).
  - `Tempo máximo de almoço`: `2 horas` (or `Retorno máximo do almoço` if in lunch break).

## 5. Scope & Out of Scope

### In Scope
- Update `WorkdaySummary` domain record with `maxExitTime` and `maxLunchReturnTime`.
- Update `WorkdaySummaryService` calculation logic:
  - Format markings strictly for present elements.
  - Compute `maxExitTime` (6h post lunch return).
  - Compute `maxLunchReturnTime` (2h post lunch out).
- Update `DiscordWorkdayEmbedBuilder` to render `Horário máximo de saída` and lunch limits.
- Update unit tests in `WorkdaySummaryServiceTest` and `DiscordWorkdayEmbedBuilderTest`.

### Out of Scope
- Modifying BMAquiosque web scraping routines.

## 6. Functional Acceptance Criteria

### AC-001: Strict List of Markings Display
**Given** a list of 1, 2, 3, or 4 time markings  
**When** `formatMarkings` is called  
**Then** only the existing markings are printed with their corresponding standard labels (`Entrada`, `Saída Almoço`, `Retorno Almoço`, `Saída`), with no extra labels or fallback text.

### AC-002: Maximum Exit Time Calculation (6 Hours After Lunch Return)
**Given** Entry at 09:00, Lunch Out at 12:00, and Lunch Return at 13:00  
**When** the summary is calculated  
**Then** `estimatedExitTime` is `18:45` (8h45 work + 1h lunch) and `maxExitTime` is `19:00` (13:00 + 6h).

### AC-003: Maximum Lunch Return Calculation (2 Hours Max Lunch)
**Given** Entry at 09:00 and Lunch Out at 12:00 (2 markings)  
**When** the summary is calculated  
**Then** `maxLunchReturnTime` is `14:00` (12:00 + 2h).

## 7. Technical Design & Implementation Checklist

### 7.1 Sequential Implementation Checklist
1. **WorkdaySummary Record Update**: Add `LocalTime maxExitTime` and `LocalTime maxLunchReturnTime`.
2. **WorkdaySummaryService Calculation Update**:
   - Compute `maxExitTime = lunchReturn.plusHours(6)` when `lunchReturn` exists.
   - Compute `maxLunchReturnTime = lunchOut.plusHours(2)` when `lunchOut` exists.
   - Ensure `formatMarkings` renders strictly up to `times.size()`.
3. **DiscordWorkdayEmbedBuilder Update**: Add embed fields for `Horário máximo de saída` and lunch limit info.
4. **Unit Test Updates**: Update and add unit tests verifying exact calculations and formatting.
5. **Verification**: Run `mvn clean test` to ensure zero regressions.
