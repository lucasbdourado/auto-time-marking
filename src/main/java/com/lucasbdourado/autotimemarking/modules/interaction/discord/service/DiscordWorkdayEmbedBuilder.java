package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdaySummary;
import com.lucasbdourado.autotimemarking.modules.calculation.service.WorkdaySummaryService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

@Component
public class DiscordWorkdayEmbedBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final WorkdaySummaryService summaryService;

    public DiscordWorkdayEmbedBuilder(WorkdaySummaryService summaryService) {
        this.summaryService = summaryService;
    }

    public MessageEmbed buildWorkDaySummaryEmbed(WorkdaySummary summary) {
        String formattedDate = summary.date() != null ? summary.date().format(DATE_FORMATTER) : "Não informada";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Resumo do ponto - " + formattedDate);
        embed.setColor(Color.GREEN);

        embed.addField("**Data**", formattedDate, false);
        embed.addField("**Marcações**", summaryService.formatMarkings(summary.markings()), false);
        embed.addField("**Tempo trabalhado**", summaryService.formatMinutes(summary.workedMinutes()), true);
        embed.addField("**Tempo restante**", summaryService.formatMinutes(summary.remainingMinutes()), true);
        embed.addField("**Horário de saída**", summaryService.formatTime(summary.estimatedExitTime()), true);

        embed.setFooter("Auto Time Marking Engine", null);

        return embed.build();
    }
}
