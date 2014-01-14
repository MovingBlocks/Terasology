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
package org.terasology.world.block.entity;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.AttachedToSurfaceFamily;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class BlockSupportRequiredSystem implements ComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void checkForAttachments(OnChangedBlock event, EntityRef entity) {
        if (event.getNewType() == BlockManager.getAir()) {
            for (Side side : Side.values()) {
                Vector3i attachedBlockPosition = side.getAdjacentPos(event.getBlockPosition());
                final Block attachedBlock = worldProvider.getBlock(attachedBlockPosition);
                final BlockFamily blockFamily = attachedBlock.getBlockFamily();
                if (blockFamily instanceof AttachedToSurfaceFamily) {
                    AttachedToSurfaceFamily attachmentFamily = (AttachedToSurfaceFamily) blockFamily;
                    final Side sideAttachedTo = attachmentFamily.getSideAttachedTo(attachedBlock);
                    if (sideAttachedTo.reverse() == side) {
                        // Block it was attached to was removed
                        blockEntityRegistry.getBlockEntityAt(attachedBlockPosition).send(
                                new DestroyBlockEvent(EntityRef.NULL, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
                    }
                }
            }
        }
    }


    @ReceiveEvent(components = {BlockComponent.class})
    public void checkForSupportRemoved(OnChangedBlock event, EntityRef entity) {
        if (event.getNewType() == BlockManager.getAir()) {
            final Vector3i blockPosition = Side.TOP.getAdjacentPos(event.getBlockPosition());
            final Block block = worldProvider.getBlock(blockPosition);
            if (block.isSupportRequired()) {
                blockEntityRegistry.getBlockEntityAt(blockPosition).send(
                        new DestroyBlockEvent(EntityRef.NULL, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
            }
        }
    }
}
