package com.lucasbdourado.autotimemarking.modules.calculation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class TimeJitterServiceTest {

    private TimeJitterService jitterService;

    @BeforeEach
    void setUp() {
        jitterService = new TimeJitterService(new Random(42));
    }

    @Test
    @DisplayName("Should return 0 when max jitter is zero or negative")
    void generateJitter_zeroOrNegative_returnsZero() {
        assertThat(jitterService.generateJitterMinutes(0)).isEqualTo(0);
        assertThat(jitterService.generateJitterMinutes(-5)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return value bounded by max jitter range")
    void generateJitter_boundedByMax() {
        int maxJitter = 15;
        for (int i = 0; i < 100; i++) {
            int jitter = jitterService.generateJitterMinutes(maxJitter);
            assertThat(jitter).isBetween(-maxJitter, maxJitter);
        }
    }

    @Test
    @DisplayName("Should apply jitter to target LocalTime")
    void applyJitter_adjustsLocalTime() {
        LocalTime baseTime = LocalTime.of(9, 0);
        LocalTime jittered = jitterService.applyJitter(baseTime, 0);
        assertThat(jittered).isEqualTo(baseTime);
    }
}
