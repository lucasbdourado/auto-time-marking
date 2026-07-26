package com.lucasbdourado.autotimemarking.modules.notification.infrastructure.discord;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda.DiscordBotInitializer;
import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationEvent;
import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationType;
import com.lucasbdourado.autotimemarking.modules.notification.infrastructure.config.NotificationProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DiscordNotificationSenderTest {

    @Mock
    private DiscordBotInitializer botInitializer;

    @Mock
    private JDA jda;

    @Mock
    private CacheRestAction<User> userRestAction;

    @Mock
    private CacheRestAction<PrivateChannel> privateChannelRestAction;

    @Mock
    private MessageCreateAction messageCreateAction;

    @Mock
    private User user;

    @Mock
    private PrivateChannel privateChannel;

    @Mock
    private TextChannel textChannel;

    private NotificationProperties properties;
    private DiscordNotificationSender notificationSender;

    @BeforeEach
    void setUp() {
        properties = new NotificationProperties();
        properties.setEnabled(true);
        properties.setDefaultChannelId("999888777");

        notificationSender = new DiscordNotificationSender(botInitializer, properties);
    }

    @Test
    @DisplayName("Should skip sending notification when properties.enabled is false")
    void sendNotification_disabled_skipsSending() {
        properties.setEnabled(false);

        notificationSender.sendNotification(NotificationEvent.success(null, "ENTRADA", "Sucesso"));

        verify(botInitializer, never()).getJda();
    }

    @Test
    @DisplayName("Should skip gracefully when JDA instance is null")
    void sendNotification_jdaNull_skipsSending() {
        when(botInitializer.getJda()).thenReturn(null);

        assertDoesNotThrow(() -> notificationSender.sendNotification(NotificationEvent.success(null, "ENTRADA", "Sucesso")));
    }

    @Test
    @DisplayName("Should send notification to fallback text channel when recipient user ID is null or blank")
    void sendNotification_noRecipientUserId_dispatchesToFallbackChannel() {
        when(botInitializer.getJda()).thenReturn(jda);
        when(jda.getTextChannelById("999888777")).thenReturn(textChannel);
        when(textChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageCreateAction);
        doAnswer(inv -> {
            Consumer<Message> success = inv.getArgument(0);
            if (success != null) success.accept(mock(Message.class));
            return null;
        }).when(messageCreateAction).queue(any(Consumer.class), any(Consumer.class));

        notificationSender.sendNotification(NotificationEvent.success(null, "ENTRADA", "Sucesso"));

        verify(jda).getTextChannelById("999888777");
        verify(textChannel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    @DisplayName("Should send notification via Direct Message (DM) when recipient user ID is provided")
    void sendNotification_withRecipientUserId_dispatchesViaDM() {
        when(botInitializer.getJda()).thenReturn(jda);
        when(jda.retrieveUserById("user123")).thenReturn(userRestAction);

        doAnswer(inv -> {
            Consumer<User> success = inv.getArgument(0);
            if (success != null) success.accept(user);
            return null;
        }).when(userRestAction).queue(any(Consumer.class), any(Consumer.class));

        when(user.openPrivateChannel()).thenReturn(privateChannelRestAction);

        doAnswer(inv -> {
            Consumer<PrivateChannel> success = inv.getArgument(0);
            if (success != null) success.accept(privateChannel);
            return null;
        }).when(privateChannelRestAction).queue(any(Consumer.class), any(Consumer.class));

        when(privateChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageCreateAction);

        doAnswer(inv -> {
            Consumer<Message> success = inv.getArgument(0);
            if (success != null) success.accept(mock(Message.class));
            return null;
        }).when(messageCreateAction).queue(any(Consumer.class), any(Consumer.class));

        notificationSender.sendNotification(NotificationEvent.success("user123", "ENTRADA", "Sucesso"));

        verify(jda).retrieveUserById("user123");
        verify(user).openPrivateChannel();
        verify(privateChannel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    @DisplayName("Should fallback to channel when DM retrieval fails")
    void sendNotification_dmFails_fallsBackToChannel() {
        when(botInitializer.getJda()).thenReturn(jda);
        when(jda.retrieveUserById("user123")).thenReturn(userRestAction);

        doAnswer(inv -> {
            Consumer<Throwable> failure = inv.getArgument(1);
            if (failure != null) failure.accept(new RuntimeException("User DM blocked"));
            return null;
        }).when(userRestAction).queue(any(Consumer.class), any(Consumer.class));

        when(jda.getTextChannelById("999888777")).thenReturn(textChannel);
        when(textChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageCreateAction);

        notificationSender.sendNotification(NotificationEvent.failure("user123", "ENTRADA", "Erro de Conexão", 2));

        verify(jda).getTextChannelById("999888777");
        verify(textChannel).sendMessageEmbeds(any(MessageEmbed.class));
    }
}
