// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.binds.general.ChatButton;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.console.ConsoleColors;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.logic.console.Message;
import org.terasology.engine.logic.console.MessageEvent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.console.suggesters.OnlineUsernameSuggester;
import org.terasology.engine.logic.console.ui.NotificationOverlay;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;
import org.terasology.nui.FontColor;

@RegisterSystem
public class ChatSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChatSystem.class);

    private static final ResourceUrn CHAT_UI = new ResourceUrn("engine:chat");
    private static final ResourceUrn CONSOLE_UI = new ResourceUrn("engine:console");

    @In
    private EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    private NotificationOverlay overlay;

    @Override
    public void initialise() {
        if (nuiManager != null) {
            overlay = nuiManager.addOverlay(NotificationOverlay.ASSET_URI, NotificationOverlay.class);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleChat(ChatButton event, EntityRef entity) {
        if (event.getState() == ButtonState.UP) {
            nuiManager.pushScreen(CHAT_UI);
            overlay.setVisible(false);
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        if (overlay == null) {
            return;
        }
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            Message message = event.getFormattedMessage();
            // show overlay only if chat and console are hidden
            if ((message.getType() == CoreMessageType.CHAT
                    || message.getType() == CoreMessageType.NOTIFICATION)
                    && !nuiManager.isOpen(CHAT_UI)
                    && !nuiManager.isOpen(CONSOLE_UI)) {
                overlay.setVisible(true);
            }
        }
    }

    @Command(runOnServer = true,
            requiredPermission = PermissionManager.CHAT_PERMISSION,
            shortDescription = "Sends a message to all other players")
    public String say(
            @Sender EntityRef sender,
            @CommandParam("message") String[] message
    ) {
        String messageToString = joinWithWhitespace(message);

        logger.debug("Received chat message from {} : '{}'", sender, messageToString);

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            client.send(new ChatMessageEvent(messageToString, sender.getComponent(ClientComponent.class).clientInfo));
        }

        return "Message sent";
    }

    private String joinWithWhitespace(String[] words) {
        return String.join(" ", words);
    }

    @Command(runOnServer = true,
            requiredPermission = PermissionManager.CHAT_PERMISSION,
            shortDescription = "Sends a private message to a specified user")
    public String whisper(
            @Sender EntityRef sender,
            @CommandParam(value = "user", suggester = OnlineUsernameSuggester.class) String username,
            @CommandParam("message") String[] message
    ) {
        String messageToString = joinWithWhitespace(message);

        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        EntityRef targetClient = null;
        boolean unique = true;

        for (EntityRef client : clients) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

            if (displayNameComponent == null) {
                continue;
            }

            if (displayNameComponent.name.equalsIgnoreCase(username)) {
                if (targetClient == null) {
                    targetClient = client;
                } else {
                    unique = false;
                    break;
                }
            }
        }

        if (!unique) {
            targetClient = null;

            for (EntityRef client : clients) {
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

                if (displayNameComponent == null) {
                    continue;
                }

                if (displayNameComponent.name.equals(username)) {
                    if (targetClient == null) {
                        targetClient = client;
                    } else {
                        return FontColor.getColored("Found more users with name '" + username + "'.", ConsoleColors.ERROR);
                    }
                }
            }
        }

        if (targetClient == null) {
            return FontColor.getColored("User with name '" + username + "' not found.", ConsoleColors.ERROR);
        }

        ClientComponent senderClientComponent = sender.getComponent(ClientComponent.class);
        ClientComponent targetClientComponent = targetClient.getComponent(ClientComponent.class);
        DisplayNameComponent targetDisplayNameComponent = targetClientComponent.clientInfo.getComponent(DisplayNameComponent.class);
        String targetMessage = FontColor.getColored("*whispering* ", ConsoleColors.ERROR)
                + FontColor.getColored(messageToString, ConsoleColors.CHAT);
        String senderMessage = "You -> " + targetDisplayNameComponent.name
                + ": " + FontColor.getColored(messageToString, ConsoleColors.CHAT);

        targetClient.send(new ChatMessageEvent(targetMessage, senderClientComponent.clientInfo));

        return senderMessage;
    }
}
