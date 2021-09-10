// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity.placement;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockPlacingSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @ReceiveEvent(components = WorldComponent.class, priority = EventPriority.PRIORITY_TRIVIAL)
    public void placeBlockInWorld(PlaceBlocks event, EntityRef world) {
        worldProvider.setBlocks(event.getBlocks());
    }
}
