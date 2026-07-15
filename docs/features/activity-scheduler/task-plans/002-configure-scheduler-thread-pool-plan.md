# Task Implementation Plan: Configure Scheduler Thread Pool

## Status

Status: Ready for Implementation

Last updated: 2026-07-15

Plan file: `docs/features/activity-scheduler/task-plans/002-configure-scheduler-thread-pool-plan.md`

## Task Reference

Task ID: `TSK-AS-002`

Task file: `docs/features/activity-scheduler/tasks/002-configure-scheduler-thread-pool.md`

Task status: `Ready`

## Feature Reference

Feature name: `activity-scheduler`

Feature file: `docs/features/activity-scheduler/feature.md`

Feature Tech Spec: `docs/features/activity-scheduler/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

List every required document, optional document, guideline, decision, localized codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/activity-scheduler/tasks/002-configure-scheduler-thread-pool.md` | Goal, Scope, Acceptance Criteria | Confirmed by source document | Primary task source |
| Feature file | `docs/features/activity-scheduler/feature.md` | Feature Goal, Scope | Confirmed by source document | Defines scheduler requirements |
| Feature Tech Spec | `docs/features/activity-scheduler/tech-spec.md` | 1. Scheduler Thread Configuration, 118: SchedulerConfig, 181-183: Performance Considerations | Confirmed by source document | Core design for custom thread pool |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions (Scheduling), 179: Inputs for Tech Spec | Confirmed by source document | Binding technology choices |
| Java Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Configuração, Responsabilidade de infraestrutura | Confirmed by source document | Configuration patterns in infrastructure |
| Package Structure | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Responsabilidades | Confirmed by source document | Infrastructure subpackage layout |
| Codebase Check | `pom.xml` | spring-boot-starter dependency | Confirmed by codebase | Validates spring-context inclusion |

## Planning Scope

This plan covers the creation of the Java package `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config` and the implementation of the configuration class `SchedulerConfig` decorated with `@Configuration` and `@EnableScheduling` that registers a customized `TaskScheduler` bean. It does not authorize the implementation of the scheduler runner, the timezone-filtering logic, or the actual marking workflow.

## Task Summary

Configure a custom `TaskScheduler` bean programmatically with a thread pool size of 1 and thread name prefix `activity-scheduler-` using Spring's task scheduling infrastructure, and enable Spring Scheduling.

## Execution Eligibility

Status: Eligible

Reason:
- The task status is `Ready` and has no dependencies (`Depends On: None`).

## Feature Context

The `activity-scheduler` feature runs background check cycles periodically. By default, Spring Boot uses a single-threaded task scheduler. To ensure thread isolation, proper thread naming (prefix `activity-scheduler-`), and clear diagnostic tracing, we define a dedicated `@Configuration` class to configure the `ThreadPoolTaskScheduler` thread pool.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach - 1. Scheduler Thread Configuration | Full | Yes | Defines the custom thread naming prefix and pool size |
| Modules and Responsibilities - SchedulerConfig | Full | Yes | Establishes the `SchedulerConfig` responsibility and the `TaskScheduler` bean registry |
| Performance Considerations - Single Thread Isolation | Full | Yes | Justifies the thread pool size of 1 |

Coverage assessment:
- Justifying Tech Spec section: Proposed Technical Approach (1) and Modules and Responsibilities.
- Tech Spec sections implemented by this task: SchedulerConfig registry.
- Gaps between task and Tech Spec: None.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 (LTS) | `technology-definition.md` | Dictates runtime and syntax features. |
| Spring Boot 3.4.x | `technology-definition.md` | Framework for `@Configuration`, `@Bean`, and `@EnableScheduling`. |
| Spring Scheduling | `technology-definition.md` | Core scheduling architecture. |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Camada de Infraestrutura | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Infrastructure Layer | Technical details like bean configuration must reside in the infrastructure layer. |
| Estrutura de Pacotes | `.agents/docs/architecture/coding-guidelines/package-structure.md` | Package Layout | The config package must be under `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config`. |

## Existing Decisions Reviewed

No existing feature, ADR, or architecture decision was relevant to this task.

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration` | Module structures | Subpackage patterns | Confirmed subpackage structure for infrastructure configurations. |
| `pom.xml` | Dependencies | Starter check | Confirmed `spring-boot-starter` is included, containing standard scheduling. |

## Confirmed Scope

- Create package directory `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config`.
- Create Java class `SchedulerConfig` under the new package.
- Decorate `SchedulerConfig` with `@Configuration` and `@EnableScheduling`.
- Define a `@Bean` method returning `TaskScheduler` (using `ThreadPoolTaskScheduler`).
- Configure the `ThreadPoolTaskScheduler` with a pool size of 1 and the thread name prefix `activity-scheduler-`.
- Verify compilation and ensure application boots successfully without errors.

## Out of Scope

- Any timezone-aware filter logic.
- Implementing the scheduled component runner (`ActivityScheduler`).
- Implementing the `MarkingWorkflow` interface itself.

## Proposed Implementation Approach

1. Create directory package structure if it does not exist: `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config`.
2. Implement `SchedulerConfig.java` to:
   - Use `@Configuration` and `@EnableScheduling`.
   - Override the default `TaskScheduler` bean by creating a bean method returning `TaskScheduler` (backed by a `ThreadPoolTaskScheduler`).
   - Call `setPoolSize(1)` and `setThreadNamePrefix("activity-scheduler-")` on the scheduler instance.
   - Initialize the scheduler via `.initialize()`.
3. Run `mvn clean compile` to ensure compilation passes.
4. Verify the application context starts up correctly without bean conflict errors.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/SchedulerConfig.java` | Create | Confirmed | Tech Spec Modules & Responsibilities | The scheduling configuration file. |

## Implementation Steps

1. Create directory: `src/main/java/com/lucasbdourado/autotimemarking/modules/scheduler/infrastructure/config/`.
2. Create file `SchedulerConfig.java` with the following implementation:
   ```java
   package com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config;

   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.scheduling.TaskScheduler;
   import org.springframework.scheduling.annotation.EnableScheduling;
   import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

   @Configuration
   @EnableScheduling
   public class SchedulerConfig {

       @Bean
       public TaskScheduler taskScheduler() {
           ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
           threadPoolTaskScheduler.setPoolSize(1);
           threadPoolTaskScheduler.setThreadNamePrefix("activity-scheduler-");
           threadPoolTaskScheduler.initialize();
           return threadPoolTaskScheduler;
       }
   }
   ```
3. Run `mvn clean compile` to verify there are no compilation errors.
4. Verify the application compiles and boots correctly without configuration collision.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| The class `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config.SchedulerConfig` exists | Full | File exists at path. |
| The configuration enables Spring Scheduling with `@EnableScheduling` | Full | `@EnableScheduling` annotation present on class definition. |
| A dedicated scheduler thread pool is configured with size 1 | Full | `setPoolSize(1)` invocation on `ThreadPoolTaskScheduler`. |
| Threads generated by the scheduler have names starting with the prefix `activity-scheduler-` | Full | `setThreadNamePrefix("activity-scheduler-")` invocation. |
| The application context starts up without errors | Full | Application startup verification. |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Compilation | Build / Manual | Verify compilation | Run `mvn clean compile` |
| Startup check | Integration / Manual | Verify Spring context boots successfully with the bean | Boot application locally |

## Dependencies

- None.

## Risks and Edge Cases

- **Bean Collision**: If another module registers a `TaskScheduler` bean, Spring Boot might report a duplicate bean error. However, this is a greenfield project with only standard configuration classes, so there is no other scheduler bean definition. If needed, Spring's auto-configuration will back off or we can define it with a specific name.

## Rollback or Recovery Notes

- Revert creation of `SchedulerConfig.java`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Decisions Created During Planning

No local feature/task decisions were created during this planning session.

## Task Planning Readiness Checklist

- [x] Task file reviewed.
- [x] Feature context reviewed.
- [x] Feature Tech Spec coverage verified.
- [x] Technology decisions reviewed.
- [x] Applicable guidelines reviewed.
- [x] Existing decisions reviewed.
- [x] Local codebase references checked when applicable.
- [x] Task dependencies checked.
- [x] Execution eligibility documented.
- [x] Blocking decisions resolved.
- [x] Local feature/task decisions documented when needed.
- [x] Architecture/global decisions routed to ADR or `resolve-architecture-blocker` when needed.
- [x] Implementation approach defined.
- [x] Acceptance criteria mapping defined.
- [x] Tests and validation strategy defined.
- [x] Risks and rollback notes documented.

## Notes for Execute Task

- The class must be named exactly `SchedulerConfig` and must reside in the package `com.lucasbdourado.autotimemarking.modules.scheduler.infrastructure.config`.
- Do not implement any scheduled runner components in this configuration class. Keep it focused on the scheduler bean registry and scheduling enabling.
