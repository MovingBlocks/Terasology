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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.general.ChatButton;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

/**
 * This system provides the ability to chat with a "say" command. Chat messages are broadcast to all players.
 *
 * @author Immortius
 */
@RegisterSystem
public class ChatSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChatSystem.class);

    @In
    private Chat chat;
    
    @In
    private EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleChat(ChatButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen("engine:chat");
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            Message message = event.getFormattedMessage();
            if (message.getType() == CoreMessageType.CHAT) {
                chat.addMessage(message.getMessage());
            }
        }
    }
    
    @Command(shortDescription = "Sends a message to all other players", runOnServer = true)
    public void say(@CommandParam("message") String message, EntityRef speaker) {
        logger.debug("Received chat message from {} : '{}'", speaker, message);
        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            client.send(new ChatMessageEvent(message, speaker.getComponent(ClientComponent.class).clientInfo));
        }
    }
}
