// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.regions;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockRegionSystem extends BaseComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    // trivial priority so that all other logic can happen to the region before erasing the blocks in the region
    @Priority(EventPriority.PRIORITY_TRIVIAL)
    @ReceiveEvent
    public void onDestroyed(DoDestroyEvent event, EntityRef entity, BlockRegionComponent blockRegion) {
        for (Vector3ic blockPosition : blockRegion.region) {
            worldProvider.setBlock(blockPosition, blockManager.getBlock(BlockManager.AIR_ID));
        }
    }
}
