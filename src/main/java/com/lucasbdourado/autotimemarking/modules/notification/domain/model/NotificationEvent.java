package com.lucasbdourado.autotimemarking.modules.notification.domain.model;

import java.time.ZonedDateTime;

public record NotificationEvent(
    String recipientDiscordUserId,
    NotificationType type,
    String title,
    String stageName,
    String message,
    ZonedDateTime timestamp,
    int retryCount
) {
    public static NotificationEvent success(String recipientId, String stageName, String message) {
        return new NotificationEvent(recipientId, NotificationType.SUCCESS, "Ponto Registrado com Sucesso! ⏰", stageName, message, ZonedDateTime.now(), 0);
    }

    public static NotificationEvent failure(String recipientId, String stageName, String errorMessage, int retries) {
        return new NotificationEvent(recipientId, NotificationType.FAILURE, "Falha no Registro de Ponto ⚠️", stageName, errorMessage, ZonedDateTime.now(), retries);
    }
}
