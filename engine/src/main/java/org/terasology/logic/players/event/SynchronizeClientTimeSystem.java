// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players.event;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.time.WorldtimeResyncEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;

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
