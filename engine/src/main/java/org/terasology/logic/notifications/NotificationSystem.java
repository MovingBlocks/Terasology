// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.notifications;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.events.ConnectedEvent;
import org.terasology.engine.network.events.DisconnectedEvent;
import org.terasology.engine.registry.In;

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
