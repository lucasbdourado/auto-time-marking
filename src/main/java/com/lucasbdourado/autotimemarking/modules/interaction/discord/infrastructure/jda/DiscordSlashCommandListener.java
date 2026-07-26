package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.service.DiscordCommandHandlerService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiscordSlashCommandListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DiscordSlashCommandListener.class);

    private final DiscordCommandHandlerService commandHandlerService;

    public DiscordSlashCommandListener(DiscordCommandHandlerService commandHandlerService) {
        this.commandHandlerService = commandHandlerService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String commandName = event.getName();

        logger.info("Received slash command '/{}' from Discord user '{}'", commandName, userId);

        String replyMessage;
        switch (commandName) {
            case "register" -> {
                commandHandlerService.registerUser(userId);
                replyMessage = "User registered successfully. Use /credentials to configure your BMAquiosque login.";
            }
            case "credentials" -> {
                OptionMapping userOpt = event.getOption("username");
                OptionMapping passOpt = event.getOption("password");
                if (userOpt == null || passOpt == null) {
                    replyMessage = "Error: Username and password parameters are required.";
                } else {
                    replyMessage = commandHandlerService.setCredentials(userId, userOpt.getAsString(), passOpt.getAsString());
                }
            }
            case "config" -> {
                OptionMapping maxEntryOpt = event.getOption("max_entry");
                OptionMapping jitterOpt = event.getOption("jitter");
                String maxEntry = maxEntryOpt != null ? maxEntryOpt.getAsString() : null;
                Integer jitter = jitterOpt != null ? jitterOpt.getAsInt() : null;
                replyMessage = commandHandlerService.configureSchedule(userId, maxEntry, jitter);
            }
            case "pause" -> replyMessage = commandHandlerService.pauseAutomation(userId);
            case "resume" -> replyMessage = commandHandlerService.resumeAutomation(userId);
            case "status" -> replyMessage = commandHandlerService.getStatus(userId);
            default -> replyMessage = "Unknown command: " + commandName;
        }

        event.reply(replyMessage)
                .setEphemeral(true)
                .queue();
    }
}
