package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class BmaquiosquePropertiesValidator {

    private static final String CREDENTIALS_ERROR = "credentials cannot be blank.";
    private static final String JITTER_MINUTES_ERROR = "jitter-minutes must be a non-negative integer.";
    private static final String MAX_ENTRY_TIME_FORMAT_ERROR = "max-entry-time must be in HH:mm format.";
    private static final String MAX_ENTRY_TIME_BOUNDARY_ERROR = "max-entry-time must be between 05:00 and 22:00.";
    private static final String TIMEZONE_ERROR = "timezone is invalid.";
    private static final String URL_ERROR = "url must be a valid HTTP or HTTPS URL.";
    private static final String SELECTORS_ERROR = "selectors cannot be blank.";

    private static final DateTimeFormatter MAX_ENTRY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime MIN_MAX_ENTRY_TIME = LocalTime.of(5, 0);
    private static final LocalTime MAX_MAX_ENTRY_TIME = LocalTime.of(22, 0);

    private final Validator validator;

    public BmaquiosquePropertiesValidator(Validator validator) {
        this.validator = validator;
    }

    public List<String> validate(BmaquiosqueProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties cannot be null");
        }

        List<String> errors = new ArrayList<>();

        validateStandardConstraints(properties, errors);
        validateTimezone(properties.getTimezone(), errors);
        validateMaxEntryTime(properties.getMaxEntryTime(), errors);
        validateUrl(properties.getUrl(), errors);
        validateSelectors(properties.getSelectors(), errors);

        return errors;
    }

    private void validateStandardConstraints(BmaquiosqueProperties properties, List<String> errors) {
        Set<ConstraintViolation<BmaquiosqueProperties>> violations = validator.validate(properties);

        for (ConstraintViolation<BmaquiosqueProperties> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();

            if ("username".equals(propertyPath) || "password".equals(propertyPath)) {
                addError(errors, CREDENTIALS_ERROR);
            } else if ("jitterMinutes".equals(propertyPath)) {
                addError(errors, JITTER_MINUTES_ERROR);
            } else if ("url".equals(propertyPath)) {
                addError(errors, URL_ERROR);
            } else if (propertyPath.startsWith("selectors")) {
                addError(errors, SELECTORS_ERROR);
            }
        }
    }

    private void validateTimezone(String timezone, List<String> errors) {
        if (timezone == null || timezone.isBlank()) {
            addError(errors, TIMEZONE_ERROR);
            return;
        }

        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            addError(errors, TIMEZONE_ERROR);
        }
    }

    private void validateMaxEntryTime(String maxEntryTime, List<String> errors) {
        if (maxEntryTime == null || maxEntryTime.isBlank()) {
            addError(errors, MAX_ENTRY_TIME_FORMAT_ERROR);
            return;
        }

        LocalTime parsedMaxEntryTime;
        try {
            parsedMaxEntryTime = LocalTime.parse(maxEntryTime, MAX_ENTRY_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            addError(errors, MAX_ENTRY_TIME_FORMAT_ERROR);
            return;
        }

        if (parsedMaxEntryTime.isBefore(MIN_MAX_ENTRY_TIME) || parsedMaxEntryTime.isAfter(MAX_MAX_ENTRY_TIME)) {
            addError(errors, MAX_ENTRY_TIME_BOUNDARY_ERROR);
        }
    }

    private void validateUrl(String url, List<String> errors) {
        if (url == null || url.isBlank()) {
            addError(errors, URL_ERROR);
            return;
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) || uri.getHost() == null) {
                addError(errors, URL_ERROR);
            }
        } catch (URISyntaxException exception) {
            addError(errors, URL_ERROR);
        }
    }

    private void validateSelectors(BmaquiosqueProperties.Selectors selectors, List<String> errors) {
        if (selectors == null
                || isBlankString(selectors.getUsername())
                || isBlankString(selectors.getPassword())
                || isBlankString(selectors.getLoginButton())
                || isBlankString(selectors.getMarkingsContainer())
                || isBlankString(selectors.getPunchButton())) {
            addError(errors, SELECTORS_ERROR);
        }
    }

    private boolean isBlankString(String value) {
        return value == null || value.isBlank();
    }

    private void addError(List<String> errors, String error) {
        if (!errors.contains(error)) {
            errors.add(error);
        }
    }
}
