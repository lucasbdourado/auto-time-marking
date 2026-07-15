package com.lucasbdourado.autotimemarking.shared.infrastructure.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingConverter extends MessageConverter {

    private static final Pattern CREDENTIAL_PATTERN = Pattern.compile(
            "(?i)['\"]?(password|pass|secret|credentials?)['\"]?\\s*[:=]\\s*['\"]?([^\\s'\",;]+)['\"]?"
    );

    private static final String MASK = "******";

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = CREDENTIAL_PATTERN.matcher(message);
        return matcher.replaceAll(matchResult -> Matcher.quoteReplacement(maskCredentialValue(message, matchResult)));
    }

    private String maskCredentialValue(String message, MatchResult matchResult) {
        String match = message.substring(matchResult.start(), matchResult.end());
        int valueStart = matchResult.start(2) - matchResult.start();
        int valueEnd = matchResult.end(2) - matchResult.start();

        return match.substring(0, valueStart) + MASK + match.substring(valueEnd);
    }
}
