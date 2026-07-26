package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.retry;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@Primary
public class RetryingTimeClockClient implements TimeClockClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryingTimeClockClient.class);
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MILLIS = 300_000L; // 5 minutes

    private final TimeClockClient delegate;
    private final int maxRetries;
    private final long retryDelayMillis;

    @Autowired
    public RetryingTimeClockClient(@Qualifier("playwrightTimeClockClient") TimeClockClient delegate) {
        this(delegate, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MILLIS);
    }

    public RetryingTimeClockClient(TimeClockClient delegate, int maxRetries, long retryDelayMillis) {
        this.delegate = delegate;
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public List<LocalTime> retrieveDailyMarkings(String username, String password) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return delegate.retrieveDailyMarkings(username, password);
            } catch (Exception exception) {
                lastException = exception;
                if (attempt < maxRetries) {
                    LOGGER.warn("Attempt {} of {} to retrieve daily markings failed for user: {}. Retrying in {} ms...",
                            attempt, maxRetries, username, retryDelayMillis, exception);
                    sleep();
                } else {
                    LOGGER.error("All {} attempts to retrieve daily markings failed for user: {}", maxRetries, username, exception);
                }
            }
        }
        throw lastException;
    }

    @Override
    public void registerMarking(String username, String password) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                delegate.registerMarking(username, password);
                return;
            } catch (Exception exception) {
                lastException = exception;
                if (attempt < maxRetries) {
                    LOGGER.warn("Attempt {} of {} to register time marking failed for user: {}. Retrying in {} ms...",
                            attempt, maxRetries, username, retryDelayMillis, exception);
                    sleep();
                } else {
                    LOGGER.error("All {} attempts to register time marking failed for user: {}", maxRetries, username, exception);
                }
            }
        }
        throw lastException;
    }

    private void sleep() {
        if (retryDelayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(retryDelayMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Retry delay sleep was interrupted", interruptedException);
        }
    }
}
