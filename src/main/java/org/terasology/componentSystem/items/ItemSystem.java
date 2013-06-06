/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.componentSystem.items;

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockItemComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.ConnectToAdjacentBlockFamily;


/**
 * TODO: Refactor use methods into events? Usage should become a separate component
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class ItemSystem implements EventHandlerSystem {
    private WorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private AudioManager audioManager;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = BlockItemComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        entity.getComponent(BlockItemComponent.class).placedEntity.destroy();
    }

    @ReceiveEvent(components = {BlockItemComponent.class, ItemComponent.class})
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);

        Side surfaceDir = Side.inDirection(event.getHitNormal());
        Side secondaryDirection = TeraMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());

        if (!placeBlock(blockItem.blockFamily, event.getTarget().getComponent(BlockComponent.class).getPosition(), surfaceDir, secondaryDirection, blockItem, item)) {
            event.cancel();
        }
    }

    @ReceiveEvent(components = ItemComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void checkCanUseItem(ActivateEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        switch (itemComp.usage) {
            case NONE:
                event.cancel();
                break;
            case ON_BLOCK:
                if (event.getTarget().getComponent(BlockComponent.class) == null) {
                    event.cancel();
                }
                break;
            case ON_ENTITY:
                if (event.getTarget().getComponent(BlockComponent.class) != null) {
                    event.cancel();
                }
                break;
        }
    }

    @ReceiveEvent(components = ItemComponent.class, priority = EventPriority.PRIORITY_TRIVIAL)
    public void usedItem(ActivateEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp.consumedOnUse) {
            itemComp.stackCount--;
            if (itemComp.stackCount == 0) {
                item.destroy();
            } else {
                item.saveComponent(itemComp);
            }
        }
    }

    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     * @return True if a block was placed
     */
    private boolean placeBlock(BlockFamily type, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection, BlockItemComponent blockItem, EntityRef item) {
        if (type == null)
            return true;

        Vector3i placementPos = new Vector3i(targetBlock);
        Block blockAtTarget = worldProvider.getBlock(targetBlock);
        if (!blockAtTarget.isReplacementAllowed())
            placementPos.add(surfaceDirection.getVector3i());

        Block blockToPlace = type.getBlockForPlacing(worldProvider, placementPos, surfaceDirection, secondaryDirection);

        if (blockToPlace == null)
            return false;

        Block blockAtPlacement = worldProvider.getBlock(placementPos);
        if (canPlaceBlock(blockToPlace, blockAtTarget, blockAtPlacement, placementPos)) {
            if (blockEntityRegistry.setBlock(placementPos, blockToPlace, blockAtPlacement, blockItem.placedEntity)) {
                audioManager.playSound(Assets.getSound("engine:PlaceBlock"), 0.5f);
                if (blockItem.placedEntity.exists()) {
                    blockItem.placedEntity = EntityRef.NULL;
                }

                item.saveComponent(new BlockComponent());
                item.send(new BlockChangedEvent(placementPos, blockToPlace, blockAtPlacement));
                return true;
            }
        }
        return false;
    }

    private boolean canPlaceBlock(Block block, Block blockAtTarget, Block blockAtPlacement, Vector3i blockPos) {
        if (!blockAtTarget.isReplacementAllowed()) {
            if (!blockAtTarget.isAttachmentAllowed()) {
                return false;
            }

            if (!blockAtPlacement.isReplacementAllowed() || blockAtPlacement.isTargetable()) {
                return false;
            }
        }
        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            return !CoreRegistry.get(BulletPhysics.class).scanArea(block.getBounds(blockPos), Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT, StandardCollisionGroup.CHARACTER)).iterator().hasNext();
        }
        return true;
    }
}
