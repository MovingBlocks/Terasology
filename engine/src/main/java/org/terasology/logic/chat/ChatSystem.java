/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.general.ChatButton;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.console.suggesters.OnlineUsernameSuggester;
import org.terasology.logic.console.ui.MiniChatOverlay;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.NUIManager;

/**
 */
@RegisterSystem
public class ChatSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChatSystem.class);

    private static final ResourceUrn CHAT_UI = new ResourceUrn("engine:chat");
    private static final ResourceUrn CONSOLE_UI = new ResourceUrn("engine:console");
    private static final ResourceUrn MINICHAT_UI = new ResourceUrn("engine:minichatOverlay");

    @In
    private EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    private MiniChatOverlay overlay;

    @Override
    public void initialise() {
        overlay = nuiManager.addOverlay(MINICHAT_UI, MiniChatOverlay.class);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleChat(ChatButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.pushScreen(CHAT_UI);
            overlay.setVisible(false);
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            Message message = event.getFormattedMessage();
            if (message.getType() == CoreMessageType.CHAT || message.getType() == CoreMessageType.NOTIFICATION) {

                // show overlay only if chat and console are hidden
                if (!nuiManager.isOpen(CHAT_UI) && !nuiManager.isOpen(CONSOLE_UI)) {
                    overlay.setVisible(true);
                }
            }
        }
    }

    @Command(runOnServer = true,
            requiredPermission = PermissionManager.CHAT_PERMISSION,
            shortDescription = "Sends a message to all other players")
    public String say(
            @Sender EntityRef sender,
            @CommandParam(value = "message") String message
    ) {
        logger.debug("Received chat message from {} : '{}'", sender, message);

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            client.send(new ChatMessageEvent(message, sender.getComponent(ClientComponent.class).clientInfo));
        }

        return "Message sent.";
    }

    @Command(runOnServer = true,
            requiredPermission = PermissionManager.CHAT_PERMISSION,
            shortDescription = "Sends a private message to a specified user")
    public String whisper(
            @Sender EntityRef sender,
            @CommandParam(value = "user", suggester = OnlineUsernameSuggester.class) String username,
            @CommandParam("message") String message
    ) {
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
                + FontColor.getColored(message, ConsoleColors.CHAT);
        String senderMessage = "You -> " + targetDisplayNameComponent.name
                + ": " + FontColor.getColored(message, ConsoleColors.CHAT);

        targetClient.send(new ChatMessageEvent(targetMessage, senderClientComponent.clientInfo));

        return senderMessage;
    }
}
