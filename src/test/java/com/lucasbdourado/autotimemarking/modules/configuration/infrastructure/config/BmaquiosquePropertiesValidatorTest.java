package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BmaquiosquePropertiesValidatorTest {

    private static final String CREDENTIALS_ERROR = "credentials cannot be blank.";
    private static final String JITTER_MINUTES_ERROR = "jitter-minutes must be a non-negative integer.";
    private static final String MAX_ENTRY_TIME_FORMAT_ERROR = "max-entry-time must be in HH:mm format.";
    private static final String MAX_ENTRY_TIME_BOUNDARY_ERROR = "max-entry-time must be between 05:00 and 22:00.";
    private static final String TIMEZONE_ERROR = "timezone is invalid.";
    private static final String URL_ERROR = "url must be a valid HTTP or HTTPS URL.";
    private static final String SELECTORS_ERROR = "selectors cannot be blank.";

    private ValidatorFactory validatorFactory;
    private BmaquiosquePropertiesValidator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = new BmaquiosquePropertiesValidator(validatorFactory.getValidator());
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldReturnCredentialsErrorWhenUsernameIsBlank(String username) {
        BmaquiosqueProperties properties = validProperties();
        properties.setUsername(username);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(CREDENTIALS_ERROR);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldReturnCredentialsErrorWhenPasswordIsBlank(String password) {
        BmaquiosqueProperties properties = validProperties();
        properties.setPassword(password);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(CREDENTIALS_ERROR);
    }

    @Test
    void shouldReturnSingleCredentialsErrorWhenUsernameAndPasswordAreBlank() {
        BmaquiosqueProperties properties = validProperties();
        properties.setUsername("");
        properties.setPassword(" ");

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(CREDENTIALS_ERROR);
    }

    @Test
    void shouldReturnJitterErrorWhenJitterMinutesIsNull() {
        BmaquiosqueProperties properties = validProperties();
        properties.setJitterMinutes(null);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(JITTER_MINUTES_ERROR);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -15})
    void shouldReturnJitterErrorWhenJitterMinutesIsNegative(Integer jitterMinutes) {
        BmaquiosqueProperties properties = validProperties();
        properties.setJitterMinutes(jitterMinutes);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(JITTER_MINUTES_ERROR);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "9:00", "25:00", "abc", "12:60"})
    void shouldReturnFormatErrorWhenMaxEntryTimeFormatIsInvalid(String maxEntryTime) {
        BmaquiosqueProperties properties = validProperties();
        properties.setMaxEntryTime(maxEntryTime);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(MAX_ENTRY_TIME_FORMAT_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"04:59", "22:01"})
    void shouldReturnBoundaryErrorWhenMaxEntryTimeIsOutOfBounds(String maxEntryTime) {
        BmaquiosqueProperties properties = validProperties();
        properties.setMaxEntryTime(maxEntryTime);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(MAX_ENTRY_TIME_BOUNDARY_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"05:00", "08:30", "22:00"})
    void shouldAcceptBoundaryAndInRangeMaxEntryTimeValues(String maxEntryTime) {
        BmaquiosqueProperties properties = validProperties();
        properties.setMaxEntryTime(maxEntryTime);

        List<String> errors = validator.validate(properties);

        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GMT+25", "Invalid/Timezone"})
    void shouldReturnTimezoneErrorWhenTimezoneIdIsInvalid(String timezone) {
        BmaquiosqueProperties properties = validProperties();
        properties.setTimezone(timezone);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(TIMEZONE_ERROR);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "ftp://invalid-url", "just-text", "http://"})
    void shouldReturnUrlErrorWhenUrlIsInvalid(String url) {
        BmaquiosqueProperties properties = validProperties();
        properties.setUrl(url);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(URL_ERROR);
    }

    @Test
    void shouldReturnSelectorsErrorWhenSelectorsIsNull() {
        BmaquiosqueProperties properties = validProperties();
        properties.setSelectors(null);

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(SELECTORS_ERROR);
    }

    @Test
    void shouldReturnSelectorsErrorWhenSelectorFieldIsBlank() {
        BmaquiosqueProperties properties = validProperties();
        properties.getSelectors().setUsername("");

        List<String> errors = validator.validate(properties);

        assertThat(errors).containsExactly(SELECTORS_ERROR);
    }

    @Test
    void shouldReturnNoErrorsWhenPropertiesAreValid() {
        BmaquiosqueProperties properties = validProperties();

        List<String> errors = validator.validate(properties);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldThrowWhenPropertiesAreNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("properties cannot be null");
    }

    private BmaquiosqueProperties validProperties() {
        BmaquiosqueProperties properties = new BmaquiosqueProperties();
        properties.setUsername("user");
        properties.setPassword("password");
        properties.setMaxEntryTime("09:00");
        properties.setJitterMinutes(5);
        properties.setTimezone("America/Sao_Paulo");
        properties.setUrl("https://bmaquiosque.example.com");
        BmaquiosqueProperties.Selectors selectors = new BmaquiosqueProperties.Selectors();
        selectors.setUsername("input[name='username']");
        selectors.setPassword("input[name='password']");
        selectors.setLoginButton("button[type='submit']");
        selectors.setMarkingsContainer(".marking-time-text");
        selectors.setPunchButton("#btn-punch");
        properties.setSelectors(selectors);
        return properties;
    }
}
