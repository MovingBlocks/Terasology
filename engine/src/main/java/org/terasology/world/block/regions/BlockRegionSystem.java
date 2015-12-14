/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.regions;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.math.geom.Vector3i;
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
        for (Vector3i blockPosition : blockRegion.region) {
            worldProvider.setBlock(blockPosition, blockManager.getBlock(BlockManager.AIR_ID));
        }
    }
}
