package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.service.DiscordCommandHandlerService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

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

    @Mock
    private InteractionHook interactionHook;

    private DiscordSlashCommandListener listener;

    private static final String USER_ID = "discord-user-999";

    @BeforeEach
    void setUp() {
        listener = new DiscordSlashCommandListener(commandHandlerService);
        lenient().when(event.getUser()).thenReturn(user);
        lenient().when(user.getId()).thenReturn(USER_ID);
        lenient().when(event.reply(anyString())).thenReturn(replyCallbackAction);
        lenient().when(replyCallbackAction.setEphemeral(anyBoolean())).thenReturn(replyCallbackAction);
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
        when(commandHandlerService.pauseAutomation(USER_ID)).thenReturn("Automação PAUSADA");

        listener.onSlashCommandInteraction(event);

        verify(commandHandlerService).pauseAutomation(USER_ID);
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    @DisplayName("Should handle /status command with ephemeral response")
    void shouldHandleStatusCommand() {
        when(event.getName()).thenReturn("status");
        when(commandHandlerService.getStatus(USER_ID)).thenReturn("Status do Usuário: ATIVO");

        listener.onSlashCommandInteraction(event);

        verify(commandHandlerService).getStatus(USER_ID);
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    @DisplayName("Should handle /ponto command using deferred reply")
    void shouldHandlePontoCommandWithDeferredReply() throws Exception {
        when(event.getName()).thenReturn("ponto");
        when(event.deferReply(true)).thenReturn(replyCallbackAction);
        doAnswer(invocation -> {
            Consumer<InteractionHook> consumer = invocation.getArgument(0);
            consumer.accept(interactionHook);
            return null;
        }).when(replyCallbackAction).queue(any());

        listener.onSlashCommandInteraction(event);

        verify(event).deferReply(true);
        verify(commandHandlerService).getWorkdaySummaryEmbed(USER_ID);
    }
}
