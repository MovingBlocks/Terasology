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

package org.terasology.componentSystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.messaging.ChatMessageEvent;
import org.terasology.events.messaging.SendChatMessage;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.manager.MessageManager;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
@RegisterSystem
public class MessagingSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(MessagingSystem.class);

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onReceiveMessage(SendChatMessage event, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            logger.info("Received message from {} : '{}'", entity, event.getMessage());
            for (EntityRef client : entityManager.listEntitiesWith(ClientComponent.class)) {
                client.send(new ChatMessageEvent(event.getMessage(), entity.getComponent(ClientComponent.class).clientInfo));
            }
        }

    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onChatMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            logger.info("Message Received : '{}'", event.getFormattedMessage());
            MessageManager.getInstance().addMessage(event.getFormattedMessage());
        }
    }
}
