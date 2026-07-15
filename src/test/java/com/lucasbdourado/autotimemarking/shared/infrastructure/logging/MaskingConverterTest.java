package com.lucasbdourado.autotimemarking.shared.infrastructure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskingConverterTest {

    private final MaskingConverter converter = new MaskingConverter();

    @ParameterizedTest
    @MethodSource("credentialPatterns")
    void shouldMaskCredentialValueWhenPatternMatches(String input, String expected) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(input);

        String maskedMessage = converter.convert(event);

        assertThat(maskedMessage).isEqualTo(expected);
    }

    @Test
    void shouldNotMaskWhenNoCredentialPatternIsPresent() {
        String input = "No credentials here, just a normal log message";
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(input);

        String maskedMessage = converter.convert(event);

        assertThat(maskedMessage).isEqualTo(input);
    }

    @Test
    void shouldReturnNullWhenMessageIsNull() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);

        String maskedMessage = converter.convert(event);

        assertThat(maskedMessage).isNull();
    }

    @Test
    void shouldReturnEmptyWhenMessageIsEmpty() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("");

        String maskedMessage = converter.convert(event);

        assertThat(maskedMessage).isEmpty();
    }

    private static Stream<Arguments> credentialPatterns() {
        return Stream.of(
                Arguments.of("password=mySecret", "password=******"),
                Arguments.of("pass: \"123\"", "pass: \"******\""),
                Arguments.of("secret=abc", "secret=******"),
                Arguments.of("credentials=test", "credentials=******"),
                Arguments.of("\"password\": \"xyz\"", "\"password\": \"******\"")
        );
    }
}
