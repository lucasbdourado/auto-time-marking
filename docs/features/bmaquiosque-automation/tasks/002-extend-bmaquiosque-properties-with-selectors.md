# Task: Extend Bmaquiosque Properties with Selectors

## Status

Ready

## Task ID

TSK-BMA-002

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Extend `BmaquiosqueProperties` configuration bean and `application.properties` to include BMAquiosque base URL and DOM selectors.

## Context

The automation engine requires a configurable URL and CSS selectors to remain resilient to BMAquiosque UI changes. These must be bound dynamically using Spring's `@ConfigurationProperties`.

## Scope

- Modify [BmaquiosqueProperties.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosqueProperties.java) to add:
  - `@NotBlank` `private String url;`
  - A nested static class `Selectors` containing:
    - `@NotBlank private String username;`
    - `@NotBlank private String password;`
    - `@NotBlank private String loginButton;`
    - `@NotBlank private String markingsContainer;`
    - `@NotBlank private String punchButton;`
  - Getter and setter methods for `url` and `selectors`.
  - Update `toString()` to include `url` and `selectors` (ensure the password remains protected).
- Modify [application.properties](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/resources/application.properties) to define:
  - `bmaquiosque.url=${BMAQUIOSQUE_URL:https://bmaquiosque.example.com}`
  - `bmaquiosque.selectors.username=${BMAQUIOSQUE_SELECTORS_USERNAME:input[name='username']}`
  - `bmaquiosque.selectors.password=${BMAQUIOSQUE_SELECTORS_PASSWORD:input[name='password']}`
  - `bmaquiosque.selectors.login-button=${BMAQUIOSQUE_SELECTORS_LOGIN_BUTTON:button[type='submit']}`
  - `bmaquiosque.selectors.markings-container=${BMAQUIOSQUE_SELECTORS_MARKINGS_CONTAINER:.marking-time-text}`
  - `bmaquiosque.selectors.punch-button=${BMAQUIOSQUE_SELECTORS_PUNCH_BUTTON:#btn-punch}`

## Out of Scope

- Modifying the properties validator or adding validator unit tests (covered in Task 003).

## Depends On

None

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `BmaquiosqueProperties` class contains the new `url` field and nested `Selectors` class with all required validation annotations.
- `application.properties` defines all the keys mapping to environment variables with appropriate fallback defaults.
- Running `mvn clean compile` succeeds.

## Implementation Notes

- Spring Boot automatically maps kebab-case properties (e.g., `login-button`) to camelCase fields (e.g., `loginButton`).
- Keep validation annotations aligned with standard JSR-380 validation.

## Validation Notes

- Run `mvn clean compile` to check that the newly modified configuration class compiles correctly.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
