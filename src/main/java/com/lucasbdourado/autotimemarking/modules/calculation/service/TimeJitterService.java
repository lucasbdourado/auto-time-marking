package com.lucasbdourado.autotimemarking.modules.calculation.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Random;

@Service
public class TimeJitterService {

    private final Random random;

    public TimeJitterService() {
        this(new Random());
    }

    public TimeJitterService(Random random) {
        this.random = random;
    }

    public int generateJitterMinutes(int maxJitterMinutes) {
        if (maxJitterMinutes <= 0) {
            return 0;
        }
        int range = (2 * maxJitterMinutes) + 1;
        return random.nextInt(range) - maxJitterMinutes;
    }

    public LocalTime applyJitter(LocalTime targetTime, int maxJitterMinutes) {
        if (targetTime == null) {
            return null;
        }
        int jitter = generateJitterMinutes(maxJitterMinutes);
        return targetTime.plusMinutes(jitter);
    }
}
