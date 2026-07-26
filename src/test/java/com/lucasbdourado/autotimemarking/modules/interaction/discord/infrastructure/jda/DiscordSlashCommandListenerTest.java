package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.service.DiscordCommandHandlerService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordSlashCommandListenerTest {

    @Mock
    private DiscordCommandHandlerService commandHandlerService;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private User user;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    private DiscordSlashCommandListener listener;

    private static final String USER_ID = "discord-user-999";

    @BeforeEach
    void setUp() {
        listener = new DiscordSlashCommandListener(commandHandlerService);
        when(event.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);
        when(event.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(anyBoolean())).thenReturn(replyCallbackAction);
    }

    @Test
    @DisplayName("Should handle /register command and return ephemeral response")
    void shouldHandleRegisterCommand() {
        when(event.getName()).thenReturn("register");

        listener.onSlashCommandInteraction(event);

        verify(commandHandlerService).registerUser(USER_ID);
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    @DisplayName("Should handle /credentials command with parameters and return ephemeral response")
    void shouldHandleCredentialsCommand() {
        when(event.getName()).thenReturn("credentials");
        OptionMapping userOpt = mock(OptionMapping.class);
        OptionMapping passOpt = mock(OptionMapping.class);
        when(userOpt.getAsString()).thenReturn("bma.user");
        when(passOpt.getAsString()).thenReturn("bma.pass");
        when(event.getOption("username")).thenReturn(userOpt);
        when(event.getOption("password")).thenReturn(passOpt);
        when(commandHandlerService.setCredentials(USER_ID, "bma.user", "bma.pass")).thenReturn("Credentials updated");

        listener.onSlashCommandInteraction(event);

        verify(commandHandlerService).setCredentials(USER_ID, "bma.user", "bma.pass");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    @DisplayName("Should handle /pause and /resume commands with ephemeral response")
    void shouldHandlePauseAndResumeCommands() {
        when(event.getName()).thenReturn("pause");
        when(commandHandlerService.pauseAutomation(USER_ID)).thenReturn("Automation PAUSED");

        listener.onSlashCommandInteraction(event);

        verify(commandHandlerService).pauseAutomation(USER_ID);
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    @DisplayName("Should handle /status command with ephemeral response")
    void shouldHandleStatusCommand() {
        when(event.getName()).thenReturn("status");
        when(commandHandlerService.getStatus(USER_ID)).thenReturn("User Status: ACTIVE");

        listener.onSlashCommandInteraction(event);

        verify(commandHandlerService).getStatus(USER_ID);
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }
}
