# Change Spec: Notification System (CAP-005)

## 1. Overview
This technical change specification defines the architecture, domain contracts, event triggers, and Discord dispatch mechanisms for **CAP-005 (Notification System)** in `auto-time-marking`. The Notification System acts as the proactive communication layer that informs users in real time via Discord when automated time-clock punches are successfully registered on BMAquiosque, or when persistent failures occur after maximum retry attempts.

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Full Product PRD | [full-product-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/full-product-prd.md#L108) | Definitive requirements for CAP-005 (F-018, F-019, US-005, US-008, UC-006, EB-009, EB-010) |
| MVP PRD | [mvp-prd.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/product/auto-time-marking/mvp-prd.md#L94) | Context on Phase 2 notification capabilities |
| Discord Bot Interface Spec | [004-discord-bot-interface.md](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/docs/changes/004-discord-bot-interface.md) | JDA integration framework and `DiscordBotInitializer` bean |
| Workflow Orchestrator | [MarkingWorkflowOrchestrator.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/workflow/service/MarkingWorkflowOrchestrator.java) | Evaluation engine initiating punch execution cycles |
| Discord Bot Initializer | [DiscordBotInitializer.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/infrastructure/jda/DiscordBotInitializer.java) | JDA instance provider for Discord communication |

## 3. Confirmed Facts vs Assumptions

### Confirmed Facts
- **F-018**: The system MUST send a Discord notification upon successful automatic marking registration.
- **F-019**: The system MUST send a Discord notification upon persistent marking failure (e.g. after retry attempts exhaust).
- **EB-010**: Successful punches generate a success notification containing details (marking stage, actual punch time, calculated target time, jitter applied).
- **EB-009**: Persistent failures generate a failure notification with warning details to prompt manual user intervention.
- Notifications utilize the existing Discord integration infrastructure powered by **JDA 5.x**.

### Assumptions & Open Questions
- Notification dispatch target:
  - If a specific `discordUserId` is present (multi-user or linked Discord user profile), notifications are delivered directly via **Discord User Direct Message (DM)**.
  - If DM delivery is disabled/blocked by the user or in single-user fallback mode, notifications are dispatched to a configured **default notification channel ID** (`discord.notification.default-channel-id`).
- When `discord.bot.enabled=false` or JDA is uninitialized, the notification service logs the message to audit log files without throwing exceptions or interrupting workflow orchestrator execution.

## 4. Current vs Expected Behavior

### Current Behavior
- `MarkingWorkflowOrchestrator` logs execution outcomes strictly to application logs (SLF4J/Logback).
- There is no proactive push communication channel to alert end users when a punch is registered or when BMAquiosque fails to respond.

### Expected Behavior
- A dedicated `modules/notification` module provides a decoupled domain port (`NotificationPort`) and application service (`NotificationService`).
- On successful punch execution in `MarkingWorkflowOrchestrator`, `NotificationPort` dispatches a **Green Discord Embed** summarizing:
  - Punch Stage (e.g. `ENTRADA`, `SAÍDA PARA ALMOÇO`, `RETORNO DO ALMOÇO`, `SAÍDA FINISH`)
  - Timestamp of execution
  - Jitter applied
  - Next anticipated punch (if applicable)
- On persistent failure (after retry limit reached or unhandled error), `NotificationPort` dispatches a **Red Discord Embed** summarizing:
  - Punch Stage attempted
  - Error reason/cause
  - Retry attempts made
  - Urgent call to action (manual punch recommendation)

## 5. Scope & Out of Scope

### In Scope
- Creation of `modules/notification` package adhering to DDD & Clean Architecture conventions.
- Domain Port `NotificationPort` and Event contracts (`NotificationEvent`, `NotificationType`, `MarkingStage`).
- Infrastructure implementation `DiscordNotificationSender` leveraging JDA (`DiscordBotInitializer.getJda()`).
- Rich Discord Embed formatting for success (Color: `#2ECC71` / GREEN) and failure (Color: `#E74C3C` / RED).
- Configuration properties binding (`discord.notification.default-channel-id`, `discord.notification.enabled`).
- Integration of `NotificationPort` into `MarkingWorkflowOrchestrator`.
- Unit tests with mocked `NotificationPort` and `JDA` events.

### Out of Scope
- Web Dashboard push notifications (Phase 3).
- Email or SMS notification channels.

## 6. Functional Acceptance Criteria

### AC-001: Success Notification Dispatch
**Given** an automated marking check cycle in `MarkingWorkflowOrchestrator`  
**When** a punch is successfully registered on BMAquiosque  
**Then** a `NotificationEvent` of type `SUCCESS` is published and sent via Discord containing a green Embed detailing the punch stage and execution timestamp.

### AC-002: Persistent Failure Notification Dispatch
**Given** an automated marking attempt that fails after max retries or encounters an unhandled exception  
**When** `MarkingWorkflowOrchestrator` catches the failure  
**Then** a `NotificationEvent` of type `FAILURE` is published and sent via Discord containing a red Embed detailing the failure reason and retry history.

### AC-003: Direct Message (DM) & Channel Fallback
**Given** a recipient user with a valid `discordUserId`  
**When** a notification is dispatched  
**Then** the system attempts delivery via Discord Direct Message (DM); if DM fails or no user ID is specified, it falls back to `discord.notification.default-channel-id`.

### AC-004: Graceful Handling when Discord Bot Disabled
**Given** application configured with `discord.bot.enabled=false` or `discord.notification.enabled=false`  
**When** a success or failure notification event occurs  
**Then** the notification sender logs a `WARN` message and completes cleanly without throwing exceptions or blocking workflow execution.

### AC-005: Formatted Embed Content
**Given** a notification event  
**When** rendered by `DiscordNotificationSender`  
**Then** the Embed MUST include:
  - Title & Color-coded side banner
  - Timestamp in `HH:mm:ss` (America/Sao_Paulo timezone)
  - Clear label of marking stage
  - Actionable status text

## 7. Technical Design & Contracts

### Package Architecture (`modules/notification`)
```
com.lucasbdourado.autotimemarking.modules.notification/
├── domain/
│   ├── model/
│   │   ├── NotificationEvent.java
│   │   ├── NotificationType.java        // SUCCESS, FAILURE, SYSTEM_INFO
│   │   └── MarkingStageNotice.java
│   └── port/
│       └── NotificationPort.java
├── infrastructure/
│   ├── config/
│   │   └── NotificationProperties.java
│   └── discord/
│       └── DiscordNotificationSender.java
└── service/
    └── NotificationService.java
```

### Domain Interfaces & Contracts

#### `NotificationPort.java`
```java
package com.lucasbdourado.autotimemarking.modules.notification.domain.port;

import com.lucasbdourado.autotimemarking.modules.notification.domain.model.NotificationEvent;

public interface NotificationPort {
    void sendNotification(NotificationEvent event);
}
```

#### `NotificationEvent.java`
```java
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
```

#### `DiscordNotificationSender.java`
```java
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
        String formattedTime = event.timestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(event.title())
            .setColor(color)
            .addField("Etapa", event.stageName() != null ? event.stageName() : "N/A", true)
            .addField("Horário", formattedTime, true)
            .addField("Detalhes", event.message(), false)
            .setFooter("Auto Time Marking Engine", null);

        if (event.retryCount() > 0) {
            builder.addField("Tentativas de Retry", String.valueOf(event.retryCount()), true);
        }

        return builder.build();
    }
}
```

### Integration into `MarkingWorkflowOrchestrator`
```java
// Inside MarkingWorkflowOrchestrator.java
if (decision.shouldPunch()) {
    try {
        timeClockClient.registerMarking(properties.getUsername(), properties.getPassword());
        notificationPort.sendNotification(
            NotificationEvent.success(
                properties.getDiscordUserId(), // User Discord ID from config
                decision.nextType().name(),
                "Marcação registrada com sucesso via BMAquiosque."
            )
        );
    } catch (Exception e) {
        notificationPort.sendNotification(
            NotificationEvent.failure(
                properties.getDiscordUserId(),
                decision.nextType().name(),
                "Erro ao registrar ponto: " + e.getMessage(),
                3
            )
        );
        throw e;
    }
}
```

## 8. Validation References & Testing Strategy

- **Unit Tests**:
  - `DiscordNotificationSenderTest`: Test success, failure, fallback channel dispatch, and disabled notification handling using mocked JDA entities (`JDA`, `User`, `PrivateChannel`, `TextChannel`).
  - `MarkingWorkflowOrchestratorTest`: Verify that `notificationPort.sendNotification(...)` is invoked with correct parameters on punch success and punch failure.
- **Verification Command**:
  ```powershell
  mvn clean test
  ```

## 9. Sequential Implementation Checklist

- [x] **Task 1**: Create `modules/notification/domain` models (`NotificationEvent`, `NotificationType`) and `NotificationPort` interface.
- [x] **Task 2**: Create `NotificationProperties` configuration binding (`discord.notification.enabled`, `discord.notification.default-channel-id`).
- [x] **Task 3**: Create `DiscordNotificationSender` implementing `NotificationPort` with JDA Embed formatting and DM/channel fallback logic.
- [x] **Task 4**: Integrate `NotificationPort` into `MarkingWorkflowOrchestrator` to emit success and failure events.
- [x] **Task 5**: Write unit test suite (`DiscordNotificationSenderTest` and update `MarkingWorkflowOrchestratorTest`).
- [x] **Task 6**: Execute full build and test verification using `mvn clean test`.
