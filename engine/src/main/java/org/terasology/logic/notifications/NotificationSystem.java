/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.logic.notifications;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.ClientComponent;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.In;

/**
 * This system provides the ability to notify one or more client about certain events.
 */
@RegisterSystem
public class NotificationSystem extends BaseComponentSystem {
    
    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent event, EntityRef entity) {
        EntityRef clientInfo = entity.getComponent(ClientComponent.class).clientInfo;
        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            client.send(NotificationMessageEvent.newJoinEvent(clientInfo));
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnect(DisconnectedEvent event, EntityRef entity) {
        EntityRef clientInfo = entity.getComponent(ClientComponent.class).clientInfo;
        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            client.send(NotificationMessageEvent.newLeaveEvent(clientInfo));
        }
    }
}
