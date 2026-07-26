package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdaySummary;
import com.lucasbdourado.autotimemarking.modules.calculation.service.WorkdaySummaryService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscordWorkdayEmbedBuilderTest {

    private DiscordWorkdayEmbedBuilder embedBuilder;

    @BeforeEach
    void setUp() {
        WorkdaySummaryService summaryService = new WorkdaySummaryService();
        embedBuilder = new DiscordWorkdayEmbedBuilder(summaryService);
    }

    @Test
    @DisplayName("Should build MessageEmbed matching reminderbot layout and green color")
    void shouldBuildWorkdaySummaryEmbed() {
        WorkdaySummary summary = new WorkdaySummary(
                LocalDate.of(2026, 7, 26),
                List.of(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(17, 45)),
                525,
                0,
                LocalTime.of(17, 45),
                LocalTime.of(19, 0),
                LocalTime.of(14, 0),
                "Jornada concluída"
        );

        MessageEmbed embed = embedBuilder.buildWorkDaySummaryEmbed(summary);

        assertNotNull(embed);
        assertEquals("Resumo do ponto - 26/07/2026", embed.getTitle());
        assertEquals(Color.GREEN, embed.getColor());
        assertNotNull(embed.getFields());
        assertEquals(7, embed.getFields().size());

        assertEquals("**Data**", embed.getFields().get(0).getName());
        assertEquals("26/07/2026", embed.getFields().get(0).getValue());

        assertEquals("**Marcações**", embed.getFields().get(1).getName());
        assertTrue(embed.getFields().get(1).getValue().contains("Entrada: 08:00"));

        assertEquals("**Tempo trabalhado**", embed.getFields().get(2).getName());
        assertEquals("08:45", embed.getFields().get(2).getValue());

        assertEquals("**Tempo restante**", embed.getFields().get(3).getName());
        assertEquals("00:00", embed.getFields().get(3).getValue());

        assertEquals("**Horário de saída**", embed.getFields().get(4).getName());
        assertEquals("17:45", embed.getFields().get(4).getValue());

        assertEquals("**Horário máximo de saída**", embed.getFields().get(5).getName());
        assertEquals("19:00", embed.getFields().get(5).getValue());

        assertEquals("**Retorno máximo do almoço**", embed.getFields().get(6).getName());
        assertEquals("14:00", embed.getFields().get(6).getValue());
    }
}
