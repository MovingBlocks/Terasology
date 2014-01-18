/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.block.items;

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius
 */
// TODO: Predict placement client-side (and handle confirm/denial)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockItemSystem implements ComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private AudioManager audioManager;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {BlockItemComponent.class, ItemComponent.class})
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        if (!event.getTarget().exists()) {
            event.consume();
            return;
        }

        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        BlockFamily type = blockItem.blockFamily;
        Side surfaceSide = Side.inDirection(event.getHitNormal());
        Side secondaryDirection = TeraMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());

        BlockComponent blockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (blockComponent == null) {
            // If there is no block there (i.e. it's a BlockGroup, we don't allow placing block, try somewhere else)
            event.consume();
            return;
        }
        Vector3i targetBlock = blockComponent.getPosition();
        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceSide.getVector3i());

        Block block = type.getBlockForPlacement(worldProvider, blockEntityRegistry, placementPos, surfaceSide, secondaryDirection);

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            // TODO: Fix this for changes.
            if (networkSystem.getMode().isAuthority()) {
                if (worldProvider.setBlock(placementPos, block) == null) {
                    // Something changed the block on another thread, cancel
                    event.consume();
                    return;
                } else {
                    item.send(new OnBlockItemPlaced(placementPos, blockEntityRegistry.getBlockEntityAt(placementPos)));
                }
            }
            event.getInstigator().send(new PlaySoundEvent(event.getInstigator(), Assets.getSound("engine:PlaceBlock"), 0.5f));
        } else {
            event.consume();
        }
    }

    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPos) {
        if (block == null) {
            return false;
        }

        Block centerBlock = worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z);
        if (!centerBlock.isAttachmentAllowed()) {
            return false;
        }

        Block adjBlock = worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z);
        if (!adjBlock.isReplacementAllowed() || adjBlock.isTargetable()) {
            return false;
        }

        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            Physics physics = CoreRegistry.get(Physics.class);
            return physics.scanArea(block.getBounds(blockPos), StandardCollisionGroup.DEFAULT, StandardCollisionGroup.CHARACTER).isEmpty();
        }
        return true;
    }
}
