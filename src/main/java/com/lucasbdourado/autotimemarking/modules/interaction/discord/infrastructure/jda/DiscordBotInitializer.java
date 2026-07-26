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
                Commands.slash("register", "Register your account for auto time marking"),
                Commands.slash("credentials", "Set your BMAquiosque login credentials")
                        .addOption(OptionType.STRING, "username", "BMA username", true)
                        .addOption(OptionType.STRING, "password", "BMA password", true),
                Commands.slash("config", "Configure schedule settings")
                        .addOption(OptionType.STRING, "max_entry", "Max entry time e.g. 09:00", false)
                        .addOption(OptionType.INTEGER, "jitter", "Jitter variation in minutes", false),
                Commands.slash("pause", "Pause auto time marking"),
                Commands.slash("resume", "Resume auto time marking"),
                Commands.slash("status", "Check current automation status and schedule")
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
