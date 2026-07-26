package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.automation.domain.TimeClockClient;
import com.lucasbdourado.autotimemarking.modules.calculation.domain.WorkdaySummary;
import com.lucasbdourado.autotimemarking.modules.calculation.service.WorkdaySummaryService;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.port.DiscordUserProfileRepository;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscordCommandHandlerService {

    private final DiscordUserProfileRepository userProfileRepository;
    private final TimeClockClient timeClockClient;
    private final WorkdaySummaryService summaryService;
    private final DiscordWorkdayEmbedBuilder embedBuilder;

    public DiscordCommandHandlerService(
            DiscordUserProfileRepository userProfileRepository,
            TimeClockClient timeClockClient,
            WorkdaySummaryService summaryService,
            DiscordWorkdayEmbedBuilder embedBuilder
    ) {
        this.userProfileRepository = userProfileRepository;
        this.timeClockClient = timeClockClient;
        this.summaryService = summaryService;
        this.embedBuilder = embedBuilder;
    }

    public DiscordUserProfile registerUser(String discordUserId) {
        return userProfileRepository.findByDiscordUserId(discordUserId)
                .orElseGet(() -> userProfileRepository.save(new DiscordUserProfile(discordUserId)));
    }

    public Optional<DiscordUserProfile> findUser(String discordUserId) {
        return userProfileRepository.findByDiscordUserId(discordUserId);
    }

    public String setCredentials(String discordUserId, String bmaUsername, String bmaPassword) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setBmaUsername(bmaUsername);
        profile.setBmaPassword(bmaPassword);
        userProfileRepository.save(profile);
        return "Credenciais do usuário '" + bmaUsername + "' do BMAquiosque atualizadas com sucesso.";
    }

    public String configureSchedule(String discordUserId, String maxEntryTime, Integer jitterMinutes) {
        DiscordUserProfile profile = registerUser(discordUserId);
        if (maxEntryTime != null && !maxEntryTime.isBlank()) {
            profile.setMaxEntryTime(maxEntryTime);
        }
        if (jitterMinutes != null && jitterMinutes >= 0) {
            profile.setJitterMinutes(jitterMinutes);
        }
        userProfileRepository.save(profile);
        return "Configuração atualizada: Horário Máximo de Entrada = " + profile.getMaxEntryTime() +
                ", Variação = " + profile.getJitterMinutes() + " minutos.";
    }

    public String pauseAutomation(String discordUserId) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setActive(false);
        userProfileRepository.save(profile);
        return "Automação PAUSADA para o seu usuário.";
    }

    public String resumeAutomation(String discordUserId) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setActive(true);
        userProfileRepository.save(profile);
        return "Automação RETOMADA para o seu usuário.";
    }

    public String getStatus(String discordUserId) {
        Optional<DiscordUserProfile> profileOpt = findUser(discordUserId);
        if (profileOpt.isEmpty()) {
            return "Usuário não registrado. Use /register para criar seu perfil.";
        }
        DiscordUserProfile profile = profileOpt.get();
        return String.format(
                "Status do Usuário:\n- Estado: %s\n- Usuário BMA: %s\n- Horário Máx. Entrada: %s\n- Variação: %d min",
                profile.isActive() ? "ATIVO" : "PAUSADO",
                profile.getBmaUsername() != null ? profile.getBmaUsername() : "Não configurado",
                profile.getMaxEntryTime(),
                profile.getJitterMinutes()
        );
    }

    public MessageEmbed getWorkdaySummaryEmbed(String discordUserId) throws Exception {
        Optional<DiscordUserProfile> profileOpt = findUser(discordUserId);
        if (profileOpt.isEmpty() || profileOpt.get().getBmaUsername() == null || profileOpt.get().getBmaUsername().isBlank()) {
            throw new IllegalArgumentException("Usuário não registrado ou credenciais do BMAquiosque não configuradas. Use /register e /credentials primeiro.");
        }

        DiscordUserProfile profile = profileOpt.get();
        List<LocalTime> times = timeClockClient.retrieveDailyMarkings(profile.getBmaUsername(), profile.getBmaPassword());
        WorkdaySummary summary = summaryService.calculateSummary(times, LocalDate.now(), LocalTime.now());

        return embedBuilder.buildWorkDaySummaryEmbed(summary);
    }
}
