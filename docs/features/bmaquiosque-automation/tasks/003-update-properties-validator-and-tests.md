# Task: Update Properties Validator and Tests

## Status

Depends on Previous Task

## Task ID

TSK-BMA-003

## Feature

`docs/features/bmaquiosque-automation/feature.md`

## Source Documents

- `docs/features/bmaquiosque-automation/feature.md`
- `docs/features/bmaquiosque-automation/tech-spec.md`
- `docs/architecture/auto-time-marking/technology-definition.md`

## Goal

Update properties validator `BmaquiosquePropertiesValidator` and its corresponding unit tests to validate the newly added BMAquiosque base URL and selectors.

## Context

The startup verification hook relies on the properties validator to prevent application boot if configuration is incomplete or invalid. The newly added URL and selectors must be validated.

## Scope

- Modify [BmaquiosquePropertiesValidator.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidator.java) to validate the new fields:
  - Validate that `url` is not blank and is a valid URL starting with `http://` or `https://`. Add error message: `"url must be a valid HTTP or HTTPS URL."`
  - Validate that the nested `selectors` object and all its fields (`username`, `password`, `loginButton`, `markingsContainer`, `punchButton`) are not blank. Add error message: `"selectors cannot be blank."`
- Modify [BmaquiosquePropertiesValidatorTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/test/java/com/lucasbdourado/autotimemarking/modules/configuration/infrastructure/config/BmaquiosquePropertiesValidatorTest.java) to add tests:
  - Test valid case with all new properties configured.
  - Test invalid/blank URL.
  - Test malformed URL (e.g. `ftp://invalid-url`, `just-text`).
  - Test blank selector fields (e.g., empty username selector, empty punch button selector).

## Out of Scope

- Implementing the Playwright client or its integration tests.

## Depends On

002-extend-bmaquiosque-properties-with-selectors.md

## Blocking Reason

None

## Required Action

None

## Acceptance Criteria

- `BmaquiosquePropertiesValidator` validates that `url` and all nested selectors are configured and valid.
- Unit tests cover both valid configurations and boundary/error cases for URL and selectors.
- Running `mvn test` passes successfully.

## Implementation Notes

- Use standard Java `java.net.URI` or `java.net.URL` validation to ensure URL validity, catching `MalformedURLException` or `URISyntaxException`.
- Standard validation constraint errors (from `@NotBlank` and `@NotNull` on fields) should be mapped to the error messages list.

## Validation Notes

- Run `mvn test -Dtest=BmaquiosquePropertiesValidatorTest` to run the updated properties tests.

## Risks

- None

## Open Questions

- None

## Notes for Plan Task

- Read all source documents before creating the implementation plan.
- Keep the plan scoped to this task's goal, dependencies, and acceptance criteria.
