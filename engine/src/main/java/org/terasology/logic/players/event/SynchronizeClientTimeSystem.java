// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.players.event;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.time.WorldtimeResyncEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SynchronizeClientTimeSystem extends BaseComponentSystem {

    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void findClients(WorldtimeResetEvent event, EntityRef entity) {
        if (entityManager.getCountOfEntitiesWith(ClientComponent.class) != 0) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {
                client.send(new WorldtimeResyncEvent(event.days));
            }
        }
    }
}
