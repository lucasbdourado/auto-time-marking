package com.lucasbdourado.autotimemarking.modules.interaction.discord.service;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.domain.model.DiscordUserProfile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscordCommandHandlerService {

    private final Map<String, DiscordUserProfile> userProfiles = new ConcurrentHashMap<>();

    public DiscordUserProfile registerUser(String discordUserId) {
        return userProfiles.computeIfAbsent(discordUserId, DiscordUserProfile::new);
    }

    public Optional<DiscordUserProfile> findUser(String discordUserId) {
        return Optional.ofNullable(userProfiles.get(discordUserId));
    }

    public String setCredentials(String discordUserId, String bmaUsername, String bmaPassword) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setBmaUsername(bmaUsername);
        profile.setBmaPassword(bmaPassword);
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
        return "Configuração atualizada: Horário Máximo de Entrada = " + profile.getMaxEntryTime() +
                ", Variação = " + profile.getJitterMinutes() + " minutos.";
    }

    public String pauseAutomation(String discordUserId) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setActive(false);
        return "Automação PAUSADA para o seu usuário.";
    }

    public String resumeAutomation(String discordUserId) {
        DiscordUserProfile profile = registerUser(discordUserId);
        profile.setActive(true);
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
}
