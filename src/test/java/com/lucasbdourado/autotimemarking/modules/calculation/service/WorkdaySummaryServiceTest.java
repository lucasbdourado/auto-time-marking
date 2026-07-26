package com.lucasbdourado.autotimemarking.modules.calculation.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdaySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkdaySummaryServiceTest {

    private WorkdaySummaryService service;
    private final LocalDate today = LocalDate.of(2026, 7, 26);

    @BeforeEach
    void setUp() {
        service = new WorkdaySummaryService();
    }

    @Test
    @DisplayName("Should return empty summary when markings list is null or empty")
    void shouldReturnEmptySummaryWhenNoMarkings() {
        WorkdaySummary summary = service.calculateSummary(List.of(), today, LocalTime.of(10, 0));

        assertNotNull(summary);
        assertEquals(0, summary.workedMinutes());
        assertEquals(525, summary.remainingMinutes());
        assertNull(summary.estimatedExitTime());
        assertEquals("Nenhuma marcação encontrada", summary.status());
        assertEquals("Nenhuma marcação encontrada", service.formatMarkings(summary.markings()));
    }

    @Test
    @DisplayName("Should calculate summary correctly for 1 marking (Entry)")
    void shouldCalculateSummaryForOneMarking() {
        List<LocalTime> times = List.of(LocalTime.of(8, 0));
        LocalTime now = LocalTime.of(12, 0); // 4h = 240 min

        WorkdaySummary summary = service.calculateSummary(times, today, now);

        assertEquals(240, summary.workedMinutes());
        assertEquals(285, summary.remainingMinutes()); // 525 - 240
        assertEquals(LocalTime.of(17, 45), summary.estimatedExitTime()); // 08:00 + 1h lunch + 8h45
        assertTrue(summary.status().contains("Em expediente"));

        String formatted = service.formatMarkings(times);
        assertEquals("Entrada: 08:00", formatted);
    }

    @Test
    @DisplayName("Should calculate summary correctly for 2 markings (Entry, Lunch Out)")
    void shouldCalculateSummaryForTwoMarkings() {
        List<LocalTime> times = List.of(LocalTime.of(8, 0), LocalTime.of(12, 0));
        LocalTime now = LocalTime.of(12, 30); // in lunch break

        WorkdaySummary summary = service.calculateSummary(times, today, now);

        assertEquals(240, summary.workedMinutes()); // 4h = 240 min
        assertEquals(285, summary.remainingMinutes()); // 525 - 240 = 285 min
        assertEquals(LocalTime.of(17, 45), summary.estimatedExitTime()); // 12:00 + 1h lunch (13:00) + 4h45 (285m) = 17:45
        assertEquals(LocalTime.of(14, 0), summary.maxLunchReturnTime()); // 12:00 + 2h
        assertEquals(LocalTime.of(20, 0), summary.maxExitTime()); // 14:00 + 6h
        assertEquals("Em intervalo de almoço", summary.status());

        String formatted = service.formatMarkings(times);
        assertTrue(formatted.contains("Entrada: 08:00"));
        assertTrue(formatted.contains("Saída Almoço: 12:00"));
    }

    @Test
    @DisplayName("Should calculate summary correctly for 3 markings (Entry, Lunch Out, Lunch Return)")
    void shouldCalculateSummaryForThreeMarkings() {
        List<LocalTime> times = List.of(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(13, 0));
        LocalTime now = LocalTime.of(15, 0); // 2h in second period -> worked 4h + 2h = 6h = 360 min

        WorkdaySummary summary = service.calculateSummary(times, today, now);

        assertEquals(360, summary.workedMinutes());
        assertEquals(165, summary.remainingMinutes()); // 525 - 360 = 165 min
        assertEquals(LocalTime.of(17, 45), summary.estimatedExitTime()); // 13:00 + 4h45 = 17:45
        assertEquals(LocalTime.of(14, 0), summary.maxLunchReturnTime()); // 12:00 + 2h
        assertEquals(LocalTime.of(19, 0), summary.maxExitTime()); // 13:00 + 6h
        assertTrue(summary.status().contains("Em expediente"));

        String formatted = service.formatMarkings(times);
        assertTrue(formatted.contains("Retorno Almoço: 13:00"));
    }

    @Test
    @DisplayName("Should calculate summary correctly for 4 markings (Full Shift)")
    void shouldCalculateSummaryForFourMarkings() {
        List<LocalTime> times = List.of(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(17, 45));
        LocalTime now = LocalTime.of(18, 0);

        WorkdaySummary summary = service.calculateSummary(times, today, now);

        assertEquals(525, summary.workedMinutes());
        assertEquals(0, summary.remainingMinutes());
        assertEquals(LocalTime.of(17, 45), summary.estimatedExitTime());
        assertEquals(LocalTime.of(19, 0), summary.maxExitTime());
        assertEquals("Jornada concluída", summary.status());

        String formatted = service.formatMarkings(times);
        assertTrue(formatted.contains("Saída: 17:45"));
    }

    @Test
    @DisplayName("Should format minutes cleanly")
    void shouldFormatMinutes() {
        assertEquals("08:45", service.formatMinutes(525));
        assertEquals("00:00", service.formatMinutes(0));
        assertEquals("01:30", service.formatMinutes(90));
        assertEquals("-00:15", service.formatMinutes(-15));
    }
}
