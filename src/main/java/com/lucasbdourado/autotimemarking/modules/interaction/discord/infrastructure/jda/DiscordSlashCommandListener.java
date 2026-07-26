package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.service.DiscordCommandHandlerService;
import net.dv8tion.jda.api.entities.MessageEmbed;
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

        switch (commandName) {
            case "ponto" -> {
                event.deferReply(true).queue(hook -> {
                    try {
                        MessageEmbed embed = commandHandlerService.punchAndGetWorkdaySummaryEmbed(userId);
                        hook.sendMessageEmbeds(embed).queue();
                    } catch (Exception e) {
                        logger.error("Failed to register punch for user {}: {}", userId, e.getMessage(), e);
                        hook.sendMessage("Erro ao registrar ponto: " + e.getMessage()).queue();
                    }
                });
                return;
            }
            case "resumo" -> {
                event.deferReply(true).queue(hook -> {
                    try {
                        MessageEmbed embed = commandHandlerService.getWorkdaySummaryEmbed(userId);
                        hook.sendMessageEmbeds(embed).queue();
                    } catch (Exception e) {
                        logger.error("Failed to generate workday summary embed for user {}: {}", userId, e.getMessage(), e);
                        hook.sendMessage("Erro ao consultar marcações de ponto: " + e.getMessage()).queue();
                    }
                });
                return;
            }
            case "registrar", "register" -> {
                commandHandlerService.registerUser(userId);
                reply(event, "Usuário registrado com sucesso. Use /credenciais para configurar seu login do BMAquiosque.");
            }
            case "credenciais", "credentials" -> {
                OptionMapping userOpt = event.getOption("usuario");
                if (userOpt == null) userOpt = event.getOption("username");

                OptionMapping passOpt = event.getOption("senha");
                if (passOpt == null) passOpt = event.getOption("password");

                if (userOpt == null || passOpt == null) {
                    reply(event, "Erro: Os parâmetros de usuário e senha são obrigatórios.");
                } else {
                    reply(event, commandHandlerService.setCredentials(userId, userOpt.getAsString(), passOpt.getAsString()));
                }
            }
            case "configurar", "config" -> {
                OptionMapping maxEntryOpt = event.getOption("horario_maximo");
                if (maxEntryOpt == null) maxEntryOpt = event.getOption("max_entry");

                OptionMapping jitterOpt = event.getOption("variacao");
                if (jitterOpt == null) jitterOpt = event.getOption("jitter");

                String maxEntry = maxEntryOpt != null ? maxEntryOpt.getAsString() : null;
                Integer jitter = jitterOpt != null ? jitterOpt.getAsInt() : null;
                reply(event, commandHandlerService.configureSchedule(userId, maxEntry, jitter));
            }
            case "pausar", "pause" -> reply(event, commandHandlerService.pauseAutomation(userId));
            case "retomar", "resume" -> reply(event, commandHandlerService.resumeAutomation(userId));
            case "status" -> reply(event, commandHandlerService.getStatus(userId));
            default -> reply(event, "Comando desconhecido: " + commandName);
        }
    }

    private void reply(SlashCommandInteractionEvent event, String message) {
        event.reply(message).setEphemeral(true).queue();
    }
}
