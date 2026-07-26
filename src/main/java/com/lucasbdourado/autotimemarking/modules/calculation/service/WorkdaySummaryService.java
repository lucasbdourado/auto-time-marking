package com.lucasbdourado.autotimemarking.modules.calculation.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdaySummary;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WorkdaySummaryService {

    public static final int WORK_MINUTES_PER_DAY = 525; // 8h45 = 525 minutos
    public static final int DEFAULT_LUNCH_MINUTES = 60; // 1 hora
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public WorkdaySummary calculateSummary(List<LocalTime> times, LocalDate date, LocalTime currentTime) {
        LocalDate summaryDate = (date != null) ? date : LocalDate.now();
        LocalTime now = (currentTime != null) ? currentTime : LocalTime.now();

        if (times == null || times.isEmpty()) {
            return new WorkdaySummary(
                    summaryDate,
                    List.of(),
                    0,
                    WORK_MINUTES_PER_DAY,
                    null,
                    "Nenhuma marcação encontrada"
            );
        }

        int count = times.size();
        long workedMinutes = 0;
        long remainingMinutes = WORK_MINUTES_PER_DAY;
        LocalTime exitTime = null;
        String status;

        switch (count) {
            case 1 -> {
                LocalTime entry = times.get(0);
                if (now.isAfter(entry)) {
                    workedMinutes = Duration.between(entry, now).toMinutes();
                }
                remainingMinutes = Math.max(0, WORK_MINUTES_PER_DAY - workedMinutes);
                exitTime = entry.plusHours(1).plusMinutes(WORK_MINUTES_PER_DAY); // entry + 1h lunch + 8h45
                status = "Em expediente (Entrada às " + entry.format(TIME_FORMATTER) + ")";
            }
            case 2 -> {
                LocalTime entry = times.get(0);
                LocalTime lunchOut = times.get(1);
                workedMinutes = Duration.between(entry, lunchOut).toMinutes();
                remainingMinutes = Math.max(0, WORK_MINUTES_PER_DAY - workedMinutes);
                exitTime = lunchOut.plusMinutes(DEFAULT_LUNCH_MINUTES).plusMinutes(remainingMinutes);
                status = "Em intervalo de almoço";
            }
            case 3 -> {
                LocalTime entry = times.get(0);
                LocalTime lunchOut = times.get(1);
                LocalTime lunchReturn = times.get(2);

                long firstPeriod = Duration.between(entry, lunchOut).toMinutes();
                long secondPeriod = 0;
                if (now.isAfter(lunchReturn)) {
                    secondPeriod = Duration.between(lunchReturn, now).toMinutes();
                }
                workedMinutes = firstPeriod + secondPeriod;
                long remainingWork = Math.max(0, WORK_MINUTES_PER_DAY - firstPeriod);
                remainingMinutes = Math.max(0, WORK_MINUTES_PER_DAY - workedMinutes);
                exitTime = lunchReturn.plusMinutes(remainingWork);
                status = "Em expediente (Retorno do almoço às " + lunchReturn.format(TIME_FORMATTER) + ")";
            }
            default -> {
                LocalTime entry = times.get(0);
                LocalTime lunchOut = times.get(1);
                LocalTime lunchReturn = times.get(2);
                LocalTime exit = times.get(3);

                long firstPeriod = Duration.between(entry, lunchOut).toMinutes();
                long secondPeriod = Duration.between(lunchReturn, exit).toMinutes();
                workedMinutes = firstPeriod + secondPeriod;
                remainingMinutes = Math.max(0, WORK_MINUTES_PER_DAY - workedMinutes);
                exitTime = exit;
                status = "Jornada concluída";
            }
        }

        return new WorkdaySummary(
                summaryDate,
                times,
                workedMinutes,
                remainingMinutes,
                exitTime,
                status
        );
    }

    public String formatMarkings(List<LocalTime> times) {
        if (times == null || times.isEmpty()) {
            return "Nenhuma marcação encontrada";
        }

        StringBuilder sb = new StringBuilder();
        String[] labels = {"Entrada: ", "Saída Almoço: ", "Retorno Almoço: ", "Saída: "};

        for (int i = 0; i < times.size(); i++) {
            LocalTime t = times.get(i);
            String label = (i < labels.length) ? labels[i] : "Marcação " + (i + 1) + ": ";
            sb.append(label).append(t.format(TIME_FORMATTER)).append("\n");
        }

        return sb.toString().trim();
    }

    public String formatMinutes(long minutes) {
        String sign = minutes < 0 ? "-" : "";
        long absoluteMinutes = Math.abs(minutes);
        long hours = absoluteMinutes / 60;
        long remainingMins = absoluteMinutes % 60;
        return String.format("%s%02d:%02d", sign, hours, remainingMins);
    }

    public String formatTime(LocalTime time) {
        if (time == null) {
            return "Não calculado";
        }
        return time.format(TIME_FORMATTER);
    }
}
