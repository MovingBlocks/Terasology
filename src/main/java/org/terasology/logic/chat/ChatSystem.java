/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.console.Console;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;

/**
 * This system provides the ability to chat with a "say" command. Chat messages are broadcast to all players.
 * @author Immortius
 */
@RegisterSystem
public class ChatSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChatSystem.class);

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Sends a message to all other players")
    public void say(@CommandParam("message") String message) {
        localPlayer.getClientEntity().send(new SendChatMessage(message));
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onReceiveMessage(SendChatMessage event, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            logger.debug("Received message from {} : '{}'", entity, event.getMessage());
            for (EntityRef client : entityManager.listEntitiesWith(ClientComponent.class)) {
                client.send(new ChatMessageEvent(event.getMessage(), entity.getComponent(ClientComponent.class).clientInfo));
            }
        }

    }
}
