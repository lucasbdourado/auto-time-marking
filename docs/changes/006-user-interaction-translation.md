# Change Spec: User Interaction Translation (CAP-006)

## 1. Overview
This technical change specification defines the translation of all user-facing interaction texts (command descriptions, status responses, error messages, and notification embeds) to **Brazilian Portuguese (PT-BR)** within `auto-time-marking`. 

Crucially, **Discord Slash Command names and option names will NOT change** (e.g. `/register`, `/credentials`, `/config`, `/pause`, `/resume`, `/status` and options `username`, `password`, `max_entry`, `jitter` remain unchanged to maintain command execution compatibility and predictable CLI syntax).

## 2. Research & Source Context
| Source | Location / Path | Purpose |
| --- | --- | --- |
| Discord Bot Initializer | [DiscordBotInitializer.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/infrastructure/jda/DiscordBotInitializer.java) | Command registration and user-facing command descriptions |
| Discord Listener | [DiscordSlashCommandListener.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/infrastructure/jda/DiscordSlashCommandListener.java) | Command routing and response message formatting |
| Discord Service | [DiscordCommandHandlerService.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/interaction/discord/service/DiscordCommandHandlerService.java) | Business logic response messages for account, schedule, pause/resume, and status |
| Discord Notification | [DiscordNotificationSender.java](file:///c:/Users/lucas.dourado/IdeaProjects/auto-time-marking/src/main/java/com/lucasbdourado/autotimemarking/modules/notification/infrastructure/discord/DiscordNotificationSender.java) | Title, fields, and footer of automated punch notifications |
| User Requirement | Chat Directive | Translate interaction texts to Portuguese while preserving command identifiers |

## 3. Confirmed Facts vs Assumptions

### Confirmed Facts
- Command names MUST remain: `register`, `credentials`, `config`, `pause`, `resume`, `status`.
- Option names MUST remain: `username`, `password`, `max_entry`, `jitter`.
- All user-facing descriptions (shown in Discord autocomplete/help) and responses returned to the user MUST be translated into clear Portuguese (PT-BR).
- Internal logging (SLF4J `logger.info`, `logger.error`) remains in English for developer/system maintenance consistency.

### Assumptions & Open Questions
- Status values in user response will map `ACTIVE` -> `ATIVO` and `PAUSED` -> `PAUSADO`.
- Missing values will display "Não configurado" instead of "Not configured".

## 4. Current vs Expected Behavior

### Current Behavior
- `/register` returns: `"User registered successfully. Use /credentials to configure your BMAquiosque login."`
- `/credentials` error returns: `"Error: Username and password parameters are required."`
- `/credentials` success returns: `"Credentials for BMAquiosque user '%s' updated successfully."`
- `/config` returns: `"Schedule updated: Max Entry Time = %s, Jitter = %d minutes."`
- `/pause` returns: `"Automation PAUSED for your user."`
- `/resume` returns: `"Automation RESUMED for your user."`
- `/status` (unregistered) returns: `"User not registered. Use /register to create your profile."`
- `/status` (registered) returns English headers: `User Status:`, `State: ACTIVE`, `BMA User:`, `Max Entry Time:`, `Jitter: X min`.
- Slash command descriptions in Discord UI are in English.

### Expected Behavior
- `/register` returns: `"Usuário registrado com sucesso. Use /credentials para configurar seu login do BMAquiosque."`
- `/credentials` error returns: `"Erro: Os parâmetros de usuário e senha são obrigatórios."`
- `/credentials` success returns: `"Credenciais do usuário '%s' do BMAquiosque atualizadas com sucesso."`
- `/config` returns: `"Configuração atualizada: Horário Máximo de Entrada = %s, Jitter = %d minutos."`
- `/pause` returns: `"Automação PAUSADA para o seu usuário."`
- `/resume` returns: `"Automação RETOMADA para o seu usuário."`
- `/status` (unregistered) returns: `"Usuário não registrado. Use /register para criar seu perfil."`
- `/status` (registered) returns Portuguese headers: `"Status do Usuário:\n- Estado: ATIVO\n- Usuário BMA: %s\n- Horário Máx. Entrada: %s\n- Jitter: %d min"`
- Slash command descriptions in Discord UI are translated to PT-BR (e.g. `"Registre sua conta para marcação automática de ponto"`).
- Discord command identifiers (`/register`, `/credentials`, `/config`, `/pause`, `/resume`, `/status`) and option names (`username`, `password`, `max_entry`, `jitter`) remain identical in English.

## 5. Scope & Out of Scope

### In Scope
- Translation of command descriptions in `DiscordBotInitializer.java`.
- Translation of reply messages in `DiscordSlashCommandListener.java`.
- Translation of service responses in `DiscordCommandHandlerService.java`.
- Translation of notification embed footer/labels in `DiscordNotificationSender.java`.
- Updating all corresponding unit tests (`DiscordCommandHandlerServiceTest`, `DiscordSlashCommandListenerTest`, `DiscordNotificationSenderTest`) to assert PT-BR strings.

### Out of Scope
- Changing Discord Slash Command names or option names.
- Translating developer-facing SLF4J logger statements.
- Web UI localization (Phase 3).

## 6. Functional Acceptance Criteria

### AC-001: Command Identifiers & Descriptions Preservation
**Given** Discord Bot initialization  
**When** slash commands are registered  
**Then** command names (`register`, `credentials`, `config`, `pause`, `resume`, `status`) and option names (`username`, `password`, `max_entry`, `jitter`) remain in English, but command and option descriptions are registered in Portuguese.

### AC-002: User Response Messages in PT-BR
**Given** a user executing any Discord slash command (`/register`, `/credentials`, `/config`, `/pause`, `/resume`, `/status`)  
**When** the bot replies to the user  
**Then** all response messages are presented in clear, grammatical Brazilian Portuguese (PT-BR).

### AC-003: User Status Representation
**Given** a user requesting `/status`  
**When** status response is generated  
**Then** the profile state is presented as `ATIVO` or `PAUSADO`, missing fields as `Não configurado`, and field titles in Portuguese.

## 7. Technical Design & String Mappings

### Summary of Text Mappings

#### Discord Command Descriptions (`DiscordBotInitializer.java`)
| Command / Option | Original (EN) | Translated (PT-BR) |
| --- | --- | --- |
| `/register` description | Register your account for auto time marking | Registre sua conta para marcação automática de ponto |
| `/credentials` description | Set your BMAquiosque login credentials | Configure suas credenciais do BMAquiosque |
| `username` option desc | BMA username | Usuário do BMAquiosque |
| `password` option desc | BMA password | Senha do BMAquiosque |
| `/config` description | Configure schedule settings | Configure os horários e preferências da automação |
| `max_entry` option desc | Max entry time e.g. 09:00 | Horário máximo de entrada (ex: 09:00) |
| `jitter` option desc | Jitter variation | Variação aleatória em minutos |
| `/pause` description | Pause auto time marking | Pausar a marcação automática de ponto |
| `/resume` description | Resume auto time marking | Retomar a marcação automática de ponto |
| `/status` description | Check current automation status and schedule | Verificar o status atual da automação e configurações |

#### Discord Command Responses (`DiscordSlashCommandListener.java` & `DiscordCommandHandlerService.java`)
| Scenario | Original (EN) | Translated (PT-BR) |
| --- | --- | --- |
| Register Success | User registered successfully. Use /credentials to configure your BMAquiosque login. | Usuário registrado com sucesso. Use /credentials para configurar seu login do BMAquiosque. |
| Credentials Missing Param | Error: Username and password parameters are required. | Erro: Os parâmetros de usuário e senha são obrigatórios. |
| Credentials Success | Credentials for BMAquiosque user '%s' updated successfully. | Credenciais do usuário '%s' do BMAquiosque atualizadas com sucesso. |
| Config Success | Schedule updated: Max Entry Time = %s, Jitter = %d minutes. | Configuração atualizada: Horário Máximo de Entrada = %s, Variação = %d minutos. |
| Pause Automation | Automation PAUSED for your user. | Automação PAUSADA para o seu usuário. |
| Resume Automation | Automation RESUMED for your user. | Automação RETOMADA para o seu usuário. |
| Status Unregistered | User not registered. Use /register to create your profile. | Usuário não registrado. Use /register para criar seu perfil. |
| Status Registered | User Status:\n- State: %s\n- BMA User: %s\n- Max Entry Time: %s\n- Jitter: %d min | Status do Usuário:\n- Estado: %s\n- Usuário BMA: %s\n- Horário Máx. Entrada: %s\n- Variação: %d min |
| Unknown Command | Unknown command: %s | Comando desconhecido: %s |

## 8. Validation References & Regression Risks
- **Automated Tests**: Run `mvn clean test` to ensure all 105+ tests compile and pass with the new PT-BR string assertions.
- **Regression Risk**: Low. Only string literal constants in user replies and command descriptions are updated. Command routing keys and option names remain untouched.

## 9. Sequential Implementation Checklist
- [x] **TASK-1**: Update command descriptions in `DiscordBotInitializer.java` to PT-BR.
- [x] **TASK-2**: Update response messages in `DiscordSlashCommandListener.java` and `DiscordCommandHandlerService.java` to PT-BR.
- [x] **TASK-3**: Update unit tests (`DiscordCommandHandlerServiceTest`, `DiscordSlashCommandListenerTest`, `DiscordNotificationSenderTest`) to verify PT-BR responses.
- [x] **TASK-4**: Run `mvn clean test` and verify clean build and test execution.
