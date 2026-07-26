package com.lucasbdourado.autotimemarking.modules.notification.domain.port;

import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationEvent;

public interface NotificationPort {
    void sendNotification(NotificationEvent event);
}
