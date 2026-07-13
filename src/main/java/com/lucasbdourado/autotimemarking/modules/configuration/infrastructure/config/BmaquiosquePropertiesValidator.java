package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

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

    private void addError(List<String> errors, String error) {
        if (!errors.contains(error)) {
            errors.add(error);
        }
    }
}
