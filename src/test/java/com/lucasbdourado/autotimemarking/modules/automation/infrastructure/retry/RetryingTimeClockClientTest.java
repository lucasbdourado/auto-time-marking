package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.retry;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryingTimeClockClientTest {

    @Mock
    private TimeClockClient delegate;

    private RetryingTimeClockClient retryingClient;

    @BeforeEach
    void setUp() {
        // Use 0ms delay for fast unit tests
        retryingClient = new RetryingTimeClockClient(delegate, 3, 0L);
    }

    @Test
    void shouldNotRetryWhenRetrieveDailyMarkingsSucceedsOnFirstAttempt() throws Exception {
        List<LocalTime> expectedMarkings = List.of(LocalTime.of(8, 0), LocalTime.of(12, 0));
        when(delegate.retrieveDailyMarkings("user", "pass")).thenReturn(expectedMarkings);

        List<LocalTime> result = retryingClient.retrieveDailyMarkings("user", "pass");

        assertThat(result).isEqualTo(expectedMarkings);
        verify(delegate, times(1)).retrieveDailyMarkings("user", "pass");
    }

    @Test
    void shouldRetryAndSucceedWhenRetrieveDailyMarkingsFailsOnFirstAttempt() throws Exception {
        List<LocalTime> expectedMarkings = List.of(LocalTime.of(8, 0));
        when(delegate.retrieveDailyMarkings("user", "pass"))
                .thenThrow(new RuntimeException("Transient connection error"))
                .thenReturn(expectedMarkings);

        List<LocalTime> result = retryingClient.retrieveDailyMarkings("user", "pass");

        assertThat(result).isEqualTo(expectedMarkings);
        verify(delegate, times(2)).retrieveDailyMarkings("user", "pass");
    }

    @Test
    void shouldPropagateExceptionWhenRetrieveDailyMarkingsFailsAllAttempts() throws Exception {
        when(delegate.retrieveDailyMarkings("user", "pass"))
                .thenThrow(new RuntimeException("Persistent failure"));

        assertThatThrownBy(() -> retryingClient.retrieveDailyMarkings("user", "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Persistent failure");

        verify(delegate, times(3)).retrieveDailyMarkings("user", "pass");
    }

    @Test
    void shouldNotRetryWhenRegisterMarkingSucceedsOnFirstAttempt() throws Exception {
        retryingClient.registerMarking("user", "pass");

        verify(delegate, times(1)).registerMarking("user", "pass");
    }

    @Test
    void shouldRetryAndSucceedWhenRegisterMarkingFailsOnFirstAttempt() throws Exception {
        doThrow(new RuntimeException("Transient error"))
                .doNothing()
                .when(delegate).registerMarking("user", "pass");

        retryingClient.registerMarking("user", "pass");

        verify(delegate, times(2)).registerMarking("user", "pass");
    }

    @Test
    void shouldPropagateExceptionWhenRegisterMarkingFailsAllAttempts() throws Exception {
        doThrow(new RuntimeException("Persistent punch failure"))
                .when(delegate).registerMarking("user", "pass");

        assertThatThrownBy(() -> retryingClient.registerMarking("user", "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Persistent punch failure");

        verify(delegate, times(3)).registerMarking("user", "pass");
    }
}
