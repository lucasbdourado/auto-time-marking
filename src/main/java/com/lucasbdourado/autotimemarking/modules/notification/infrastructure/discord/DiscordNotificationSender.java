package com.lucasbdourado.autotimemarking.modules.notification.infrastructure.discord;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda.DiscordBotInitializer;
import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationEvent;
import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationType;
import com.lucasbdourado.autotimemarking.modules.notification.domain.port.NotificationPort;
import com.lucasbdourado.autotimemarking.modules.notification.infrastructure.config.NotificationProperties;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

@Component
public class DiscordNotificationSender implements NotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(DiscordNotificationSender.class);

    private final DiscordBotInitializer botInitializer;
    private final NotificationProperties properties;

    public DiscordNotificationSender(DiscordBotInitializer botInitializer, NotificationProperties properties) {
        this.botInitializer = botInitializer;
        this.properties = properties;
    }

    @Override
    public void sendNotification(NotificationEvent event) {
        if (!properties.isEnabled()) {
            logger.info("Notification system is disabled. Skipping dispatch for event: {}", event.title());
            return;
        }

        JDA jda = botInitializer.getJda();
        if (jda == null) {
            logger.warn("JDA instance is not available. Skipping Discord notification for event: {}", event.title());
            return;
        }

        MessageEmbed embed = buildEmbed(event);

        if (event.recipientDiscordUserId() != null && !event.recipientDiscordUserId().isBlank()) {
            jda.retrieveUserById(event.recipientDiscordUserId()).queue(
                user -> user.openPrivateChannel().queue(
                    channel -> channel.sendMessageEmbeds(embed).queue(
                        success -> logger.info("Notification sent via DM to user {}", event.recipientDiscordUserId()),
                        error -> sendToFallbackChannel(jda, embed, "DM failed: " + error.getMessage())
                    ),
                    error -> sendToFallbackChannel(jda, embed, "Could not open DM channel: " + error.getMessage())
                ),
                error -> sendToFallbackChannel(jda, embed, "User not found: " + error.getMessage())
            );
        } else {
            sendToFallbackChannel(jda, embed, "No recipient user ID provided.");
        }
    }

    private void sendToFallbackChannel(JDA jda, MessageEmbed embed, String reason) {
        String channelId = properties.getDefaultChannelId();
        if (channelId == null || channelId.isBlank()) {
            logger.warn("Fallback channel not configured. Cannot send notification. Reason: {}", reason);
            return;
        }

        var channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.sendMessageEmbeds(embed).queue(
                s -> logger.info("Notification sent to fallback channel {}", channelId),
                e -> logger.error("Failed to send notification to fallback channel {}: {}", channelId, e.getMessage())
            );
        } else {
            logger.warn("Fallback text channel '{}' not found in Discord gateway.", channelId);
        }
    }

    private MessageEmbed buildEmbed(NotificationEvent event) {
        Color color = event.type() == NotificationType.SUCCESS ? Color.GREEN : Color.RED;
        String formattedTime = (event.timestamp() != null ? event.timestamp() : java.time.ZonedDateTime.now())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(event.title())
            .setColor(color)
            .addField("Etapa", event.stageName() != null ? event.stageName() : "N/A", true)
            .addField("Horário", formattedTime, true)
            .addField("Detalhes", event.message() != null ? event.message() : "", false)
            .setFooter("Auto Time Marking Engine", null);

        if (event.retryCount() > 0) {
            builder.addField("Tentativas de Retry", String.valueOf(event.retryCount()), true);
        }

        return builder.build();
    }
}
