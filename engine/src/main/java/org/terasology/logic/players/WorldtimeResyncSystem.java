// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.time.WorldtimeResyncEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

@RegisterSystem(RegisterMode.CLIENT)
public class WorldtimeResyncSystem extends BaseComponentSystem {

    @In
    private WorldProvider world;

    @ReceiveEvent(components = ClientComponent.class)
    public void resyncWorldTime(WorldtimeResyncEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            world.getTime().setDays(event.days);
        }
    }
}
