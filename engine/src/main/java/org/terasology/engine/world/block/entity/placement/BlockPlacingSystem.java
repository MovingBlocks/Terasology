// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity.placement;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockPlacingSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @Priority(EventPriority.PRIORITY_TRIVIAL)
    @ReceiveEvent(components = WorldComponent.class)
    public void placeBlockInWorld(PlaceBlocks event, EntityRef world) {
        worldProvider.setBlocks(event.getBlocks());
    }
}
