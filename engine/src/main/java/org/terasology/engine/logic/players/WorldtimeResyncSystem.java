// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.time.WorldtimeResyncEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;

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
