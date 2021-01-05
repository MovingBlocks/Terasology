// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block.regions;

import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockRegionSystem extends BaseComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    // trivial priority so that all other logic can happen to the region before erasing the blocks in the region
    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onDestroyed(DoDestroyEvent event, EntityRef entity, BlockRegionComponent blockRegion) {
        for (Vector3ic blockPosition : blockRegion.region) {
            worldProvider.setBlock(blockPosition, blockManager.getBlock(BlockManager.AIR_ID));
        }
    }
}
