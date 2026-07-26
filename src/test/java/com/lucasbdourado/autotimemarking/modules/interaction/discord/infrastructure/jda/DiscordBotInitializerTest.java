package com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.jda;

import com.lucasbdourado.autotimemarking.modules.interaction.discord.infrastructure.config.DiscordBotProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class DiscordBotInitializerTest {

    @Mock
    private DiscordSlashCommandListener slashCommandListener;

    @Test
    @DisplayName("Should skip initialization when bot is disabled")
    void shouldSkipInitializationWhenDisabled() {
        DiscordBotProperties properties = new DiscordBotProperties();
        properties.setEnabled(false);

        DiscordBotInitializer initializer = new DiscordBotInitializer(properties, slashCommandListener);
        initializer.init();

        assertNull(initializer.getJda());
    }

    @Test
    @DisplayName("Should handle shutdown gracefully when JDA is null")
    void shouldShutdownGracefullyWhenJdaIsNull() {
        DiscordBotProperties properties = new DiscordBotProperties();
        DiscordBotInitializer initializer = new DiscordBotInitializer(properties, slashCommandListener);

        assertDoesNotThrow(initializer::shutdown);
    }
}
