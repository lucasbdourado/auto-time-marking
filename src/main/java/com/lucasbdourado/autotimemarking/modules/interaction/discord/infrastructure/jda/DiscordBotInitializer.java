package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.config.DiscordBotProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiscordBotInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DiscordBotInitializer.class);

    private final DiscordBotProperties properties;
    private final DiscordSlashCommandListener slashCommandListener;
    private JDA jda;

    public DiscordBotInitializer(DiscordBotProperties properties, DiscordSlashCommandListener slashCommandListener) {
        this.properties = properties;
        this.slashCommandListener = slashCommandListener;
    }

    @PostConstruct
    public void init() {
        if (!properties.isEnabled() || properties.getToken() == null || properties.getToken().isBlank()) {
            logger.info("Discord Bot is disabled or token is missing. Skipping Discord bot initialization.");
            return;
        }

        try {
            logger.info("Initializing Discord Bot client via JDA...");
            jda = JDABuilder.createDefault(properties.getToken())
                    .addEventListeners(slashCommandListener)
                    .build();

            jda.awaitReady();
            registerCommands();
            logger.info("Discord Bot started and commands registered successfully as '{}'", jda.getSelfUser().getAsTag());
        } catch (Exception e) {
            logger.error("Failed to initialize Discord Bot: {}", e.getMessage(), e);
        }
    }

    private void registerCommands() {
        if (jda == null) return;

        var commandsList = java.util.List.of(
                Commands.slash("register", "Registre sua conta para marcação automática de ponto"),
                Commands.slash("credentials", "Configure suas credenciais do BMAquiosque")
                        .addOption(OptionType.STRING, "username", "Usuário do BMAquiosque", true)
                        .addOption(OptionType.STRING, "password", "Senha do BMAquiosque", true),
                Commands.slash("config", "Configure os horários e preferências da automação")
                        .addOption(OptionType.STRING, "max_entry", "Horário máximo de entrada (ex: 09:00)", false)
                        .addOption(OptionType.INTEGER, "jitter", "Variação aleatória em minutos", false),
                Commands.slash("pause", "Pausar a marcação automática de ponto"),
                Commands.slash("resume", "Retomar a marcação automática de ponto"),
                Commands.slash("status", "Verificar o status atual da automação e configurações"),
                Commands.slash("ponto", "Exibe o resumo das marcações de ponto do dia atual"),
                Commands.slash("resumo", "Exibe o resumo das marcações de ponto do dia atual")
        );

        if (properties.getGuildId() != null && !properties.getGuildId().isBlank()) {
            var guild = jda.getGuildById(properties.getGuildId());
            if (guild != null) {
                guild.updateCommands().addCommands(commandsList).queue();
                logger.info("Registered Slash Commands for Guild ID '{}'", properties.getGuildId());
            } else {
                jda.updateCommands().addCommands(commandsList).queue();
                logger.warn("Guild ID '{}' not found. Registered Slash Commands globally instead.", properties.getGuildId());
            }
        } else {
            jda.updateCommands().addCommands(commandsList).queue();
            logger.info("Registered Slash Commands globally.");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            logger.info("Shutting down Discord Bot JDA connection...");
            jda.shutdown();
        }
    }

    public JDA getJda() {
        return jda;
    }
}
