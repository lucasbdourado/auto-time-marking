# Task Implementation Plan: Implement Bmaquiosque Properties

## Status

Status: Ready for Implementation

Last updated: 2026-07-13

Plan file: `docs/features/single-user-configuration/task-plans/002-implement-bmaquiosque-properties-plan.md`

## Task Reference

Task ID: `TSK-SUC-002`

Task file: `docs/features/single-user-configuration/tasks/002-implement-bmaquiosque-properties.md`

Task status: `Depends on Previous Task` (Prerequisite is complete)

## Feature Reference

Feature name: `single-user-configuration`

Feature file: `docs/features/single-user-configuration/feature.md`

Feature Tech Spec: `docs/features/single-user-configuration/tech-spec.md`

Technology definition: `docs/architecture/auto-time-marking/technology-definition.md`

## Source Documents

| Source | Path or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/features/single-user-configuration/tasks/002-implement-bmaquiosque-properties.md` | Goal, Scope, Acceptance Criteria | Confirmed | Defines the fields and validation annotations |
| Feature file | `docs/features/single-user-configuration/feature.md` | Feature Goal, Scope | Confirmed | General functional context |
| Feature Tech Spec | `docs/features/single-user-configuration/tech-spec.md` | Proposed Technical Approach, Data Contracts, Security | Confirmed | Technical approach and packages |
| Technology Definition | `docs/architecture/auto-time-marking/technology-definition.md` | Confirmed Technology Decisions | Confirmed | Tech stack and environment mapping constraints |
| Guidelines | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Configuração, O que pode existir em Infrastructure | Confirmed | Coding standards for configuration classes |
| Technology Reference | `docs/references/auto-time-marking/technologies/springboot.md` | Usage Guidelines | Confirmed | Env var mapping rules |

## Planning Scope

This plan covers `TSK-SUC-002` only, which involves:
- Creating the `@ConfigurationProperties` binding bean class `BmaquiosqueProperties` with JSR-380 validation annotations.
- Mapping these properties in `src/main/resources/application.properties` to environment variables with appropriate fallback values.
It does not authorize implementation or cover logical validation logic (handled in Task 003) or startup hooks (handled in Task 004).

## Task Summary

Create the `BmaquiosqueProperties` class under the package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config` using `@ConfigurationProperties(prefix = "bmaquiosque")`, validate fields using JSR-380 (`@NotBlank`, `@NotNull`, `@Min(0)`), and map them to environment variables with fallback values in `application.properties`.

## Execution Eligibility

Status: Eligible

Reason:
- The previous task `001-setup-project-chassis.md` has been successfully implemented (`pom.xml` and folder chassis exist).

## Feature Context

The `single-user-configuration` feature loads the BMAquiosque credentials and settings from external sources at startup. `BmaquiosqueProperties` is the strongly-typed container bean that holds these values, exposing them to other modules (scheduler, automation, calculation) via Spring DI.

## Tech Spec Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Approach | Partial | Class definition and basic annotations | Custom validator and lifecycle hook are out of scope |
| Architecture Notes | Partial | Class package placement | Injecting into other components is out of scope |
| Data Contracts | Partial | Schema properties binding and JSR-380 constraints | Timezone check and logical checks are out of scope |
| Security and Permissions | Full | Mapping keys to environment variables in properties file | Log masking is out of scope |

Coverage assessment:
- Justifying Tech Spec section: `Proposed Technical Approach`
- Tech Spec sections implemented by this task: `Data Contracts` (schema keys and basic rules), `Security and Permissions` (credential environment mapping)
- Gaps between task and Tech Spec: Custom validator (`BmaquiosquePropertiesValidator`), startup lifecycle hook (`ConfigurationVerificationHook`), log masking, and tests are deferred to future tasks.
- Dependencies not specified by the Tech Spec: None.

## Technology Decisions Used

| Decision | Source | Impact on This Task |
| --- | --- | --- |
| Java 21 | `technology-definition.md` | Code written must target Java 21 compiler compatibility. |
| Spring Boot 3.4.1 | `technology-definition.md` | Standard Spring Boot annotations (`@ConfigurationProperties`, `@Configuration`, `@Validated`) and validation starter. |
| Properties Format | `technology-definition.md` | Configuration defined in `application.properties`. |

## Applicable Guidelines

| Guideline | Path | Applies To | How It Affects This Plan |
| --- | --- | --- | --- |
| Java/Clean Architecture - Infrastructure | `.agents/docs/architecture/coding-guidelines/infrastructure-layer.md` | Configuration placement | Places class files under the infrastructure layer in the `configuration` module. |
| Spring Boot Properties Reference | `docs/references/auto-time-marking/technologies/springboot.md` | Config binding and env mapping | Guides the mapping syntax `prefix.key=${ENV_VAR:default_value}`. |

## Existing Decisions Reviewed

| Decision | Path | Relevance |
| --- | --- | --- |
| Confirmed technology stack | `docs/architecture/auto-time-marking/technology-definition.md` | Mandates use of `@ConfigurationProperties`, JSR-380, and `application.properties`. |

## Local Codebase References

| Path or Area | What Was Checked | Relevance | Notes |
| --- | --- | --- | --- |
| `src/main/resources/application.properties` | File existence and contents | Target file to configure properties mapping | File is currently empty. |
| `pom.xml` | Dependency declarations | Ensures `spring-boot-starter-validation` is present | Dependency is already declared. |

## Confirmed Scope

- Create class `BmaquiosqueProperties` under package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
- Annotate the class with:
  - `@Configuration` (to register as a Spring bean)
  - `@ConfigurationProperties(prefix = "bmaquiosque")`
  - `@Validated` (enables JSR-380 validation on `@ConfigurationProperties` binding)
- Declare fields:
  - `username` (String)
  - `password` (String)
  - `maxEntryTime` (String)
  - `jitterMinutes` (Integer)
  - `timezone` (String) with default value `"America/Sao_Paulo"`
- Add JSR-380 annotations:
  - `username` -> `@NotBlank`
  - `password` -> `@NotBlank`
  - `jitterMinutes` -> `@NotNull`, `@Min(0)`
- Provide standard getter and setter methods for all fields.
- Update `src/main/resources/application.properties` with properties mapped to environment variables and fallback defaults:
  - `bmaquiosque.username=${BMAQUIOSQUE_USERNAME:}`
  - `bmaquiosque.password=${BMAQUIOSQUE_PASSWORD:}`
  - `bmaquiosque.max-entry-time=${BMAQUIOSQUE_MAX_ENTRY_TIME:}`
  - `bmaquiosque.jitter-minutes=${BMAQUIOSQUE_JITTER_MINUTES:}`
  - `bmaquiosque.timezone=${BMAQUIOSQUE_TIMEZONE:America/Sao_Paulo}`

## Out of Scope

- Implementing logical validation for `maxEntryTime` or timezone in `BmaquiosquePropertiesValidator` (deferred to TSK-SUC-003).
- Implementing the startup hook `ConfigurationVerificationHook` (deferred to TSK-SUC-004).
- Logging format implementation or credential masking (deferred to TSK-SUC-005).
- Creating test suites or test cases (deferred to TSK-SUC-006).

## Proposed Implementation Approach

1. Create package `com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config`.
2. Implement class `BmaquiosqueProperties` under this package.
3. Apply required annotations (`@Configuration`, `@ConfigurationProperties`, `@Validated`) and JSR-380 annotations on the fields.
4. Populate `src/main/resources/application.properties` with the property keys mapped to standard system environment variables, specifying `America/Sao_Paulo` as the default fallback for `bmaquiosque.timezone`.

## Expected Files or Areas

| Expected File or Area | Expected Action | Confidence | Source | Notes |
| --- | --- | --- | --- | --- |
| `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java` | Create | Confirmed | Tech Spec, Task Scope | New configuration binding bean |
| `src/main/resources/application.properties` | Modify | Confirmed | Tech Spec, Task Scope | Map properties to system environment variables |

## Implementation Steps

1. Create directory `src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config` if it does not exist.
2. Create `BmaquiosqueProperties.java` in this directory with package declaration, imports, class annotations, fields, validation annotations, and getters/setters.
3. Edit `src/main/resources/application.properties` to add:
   ```properties
   bmaquiosque.username=${BMAQUIOSQUE_USERNAME:}
   bmaquiosque.password=${BMAQUIOSQUE_PASSWORD:}
   bmaquiosque.max-entry-time=${BMAQUIOSQUE_MAX_ENTRY_TIME:}
   bmaquiosque.jitter-minutes=${BMAQUIOSQUE_JITTER_MINUTES:}
   bmaquiosque.timezone=${BMAQUIOSQUE_TIMEZONE:America/Sao_Paulo}
   ```
4. Verify the setup compiles correctly using Maven.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Coverage | Validation Evidence |
| --- | --- | --- |
| Class `BmaquiosqueProperties` exists under the correct package and contains the specified fields and JSR-380 validation annotations | Full | Java class will be checked and verified |
| `application.properties` specifies the keys and maps them to environment variables | Full | Property file entries will be checked and verified |
| The project compiles successfully | Full | Run `mvn clean compile` and check for green build status |

## Tests and Validation Strategy

| Test or Validation | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Build check | Compilation | Check that the newly added configuration class compiles correctly | Execute `mvn clean compile` command |

## Dependencies

- Requires TSK-SUC-001 (Implemented).

## Risks and Edge Cases

- **Missing Environment Variables**: If environment variables are absent during local development, Spring Boot startup validation might fail if standard fallbacks are not provided. The properties are configured to bind empty values when environment variables are not set, which will cause validation to fail at runtime as intended (credentials cannot be blank), but will not fail compilation or context initialization if appropriate test configurations are used.

## Rollback or Recovery Notes

- Delete `BmaquiosqueProperties.java` and revert changes to `application.properties`.

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
- [x] Acceptance criteria mapped.
- [x] Tests and validation strategy defined.
- [x] Risks and rollback notes documented.

## Notes for Execute Task

- The class `BmaquiosqueProperties` must be marked with `@ConfigurationProperties(prefix = "bmaquiosque")`, `@Configuration`, and `@Validated`.
- In `application.properties`, do not leave placeholders; map them directly to standard uppercase environment variables using `${VAR:}` syntax.
