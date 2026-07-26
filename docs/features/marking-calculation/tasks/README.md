# Feature Tasks Index: marking-calculation

## Feature Reference

`docs/features/marking-calculation/feature.md`

## Feature Technical Specification

`docs/features/marking-calculation/tech-spec.md`

## Tasks List

| Task ID | Task Document | Title | Status | Dependencies |
| --- | --- | --- | --- | --- |
| TSK-MC-001 | [001-define-marking-domain-models.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tasks/001-define-marking-domain-models.md) | Define Marking Domain Models | Pending | None |
| TSK-MC-002 | [002-implement-workday-calculation-engine.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tasks/002-implement-workday-calculation-engine.md) | Implement Workday Calculation Engine | Pending | TSK-MC-001 |
| TSK-MC-003 | [003-implement-time-jitter-service.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tasks/003-implement-time-jitter-service.md) | Implement Time Jitter Service | Pending | TSK-MC-001 |
| TSK-MC-004 | [004-implement-marking-workflow-orchestrator.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tasks/004-implement-marking-workflow-orchestrator.md) | Implement Marking Workflow Orchestrator | Pending | TSK-MC-001, TSK-MC-002, TSK-MC-003, TSK-BMA-004 |
| TSK-MC-005 | [005-write-calculation-unit-tests.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tasks/005-write-calculation-unit-tests.md) | Write Calculation Unit Tests | Pending | TSK-MC-002, TSK-MC-003, TSK-MC-004 |
| TSK-MC-999 | [999-verify-feature-completion.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/features/marking-calculation/tasks/999-verify-feature-completion.md) | Verify Feature Completion | Pending | TSK-MC-001 through TSK-MC-005 |

## Implementation Order

1. `TSK-MC-001`: Define Marking Domain Models (`MarkingType`, `MarkingRecord`, `WorkdayState`, `PunchDecision`).
2. `TSK-MC-002`: Implement Workday Calculation Engine (`MarkingCalculatorService`).
3. `TSK-MC-003`: Implement Time Jitter Service (`TimeJitterService`).
4. `TSK-MC-004`: Implement Marking Workflow Orchestrator (`MarkingWorkflowOrchestrator`).
5. `TSK-MC-005`: Write Calculation Unit Tests (`MarkingCalculatorTest`, `TimeJitterServiceTest`, `MarkingWorkflowOrchestratorTest`).
6. `TSK-MC-999`: Verify Feature Completion (`mvn clean compile test`).
