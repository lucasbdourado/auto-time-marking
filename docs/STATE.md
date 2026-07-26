# Harness Execution State

## Current Feature / Change

`009-markings-display-and-limits`

## Current Change Spec

- `docs/changes/009-markings-display-and-limits.md`

## Current Status

Change Spec `009-markings-display-and-limits` Implemented & Verified.

## Last Completed Step

Implemented CAP-009 (Markings Display & Workday Shift Limits):
- Updated `WorkdaySummary` record with `maxExitTime` and `maxLunchReturnTime` fields.
- Updated `WorkdaySummaryService` calculation logic to calculate `maxExitTime` (6h post-lunch return) and `maxLunchReturnTime` (2h max lunch break).
- Preserved strict list of markings display in `formatMarkings` (only registered markings rendered).
- Updated `DiscordWorkdayEmbedBuilder` to render `Horário máximo de saída` and `Retorno máximo do almoço` fields.
- Updated unit test suites `WorkdaySummaryServiceTest` and `DiscordWorkdayEmbedBuilderTest`.

## Current Blocker

None.

## Required Next Action

Awaiting next task or user guidance.

## Safe Resume Point

`docs/STATE.md`

## Last Updated

`2026-07-26 20:19:00 -03:00`
