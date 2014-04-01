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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockSupportRequiredComponent;
import org.terasology.world.block.family.AttachedToSurfaceFamily;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class BlockSupportRequiredSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private PrefabManager prefabManager;

    private boolean midDestruction;
    private EntityRef gatheringEntity;

    @ReceiveEvent(components = {BlockComponent.class})
    public void checkForSupportRemoved(OnChangedBlock event, EntityRef entity) {
        if (event.getNewType() == BlockManager.getAir()) {
            boolean initialEvent = !midDestruction;

            if (initialEvent) {
                midDestruction = true;
                gatheringEntity = entityManager.create();
                gatheringEntity.addComponent(new InventoryComponent(20));
            }
            try {
                for (Side side : Side.values()) {
                    validateSupportForBlockOnSide(event.getBlockPosition(), side);
                }

                if (initialEvent) {
                    PickupBuilder pickupBuilder = new PickupBuilder(entityManager);
                    for (int i = 0; i < 20; i++) {
                        EntityRef item = inventoryManager.getItemInSlot(gatheringEntity, i);
                        if (item.exists()) {
                            pickupBuilder.createPickupFor(item, event.getBlockPosition().toVector3f(), 60, true);
                        }
                    }
                }
            } finally {
                if (initialEvent) {
                    midDestruction = false;
                    gatheringEntity.destroy();
                }
            }
        }
    }

    private void validateSupportForBlockOnSide(Vector3i replacedBlockPosition, Side side) {
        final Vector3i blockPosition = side.getAdjacentPos(replacedBlockPosition);
        boolean destroyBlock = false;
        Block blockAtPosition = worldProvider.getBlock(blockPosition);
        if (side == Side.TOP && blockAtPosition.isSupportRequired()) {
            destroyBlock = true;
        } else if (blockAtPosition.getBlockFamily() instanceof AttachedToSurfaceFamily
                && ((AttachedToSurfaceFamily) blockAtPosition.getBlockFamily()).getSideAttachedTo(blockAtPosition) == side) {
            destroyBlock = true;
        } else {
            EntityRef blockEntity = blockEntityRegistry.getExistingBlockEntityAt(blockPosition);
            if (blockEntity.exists()) {
                BlockSupportRequiredComponent supportComponent = blockEntity.getComponent(BlockSupportRequiredComponent.class);
                if (supportComponent != null) {
                    if (side.isHorizontal() && supportComponent.sideAllowed
                            && !hasSupport(blockPosition, supportComponent)) {
                        destroyBlock = true;
                    } else if (!side.isHorizontal() && (supportComponent.topAllowed || supportComponent.bottomAllowed)
                            && !hasSupport(blockPosition, supportComponent)) {
                        destroyBlock = true;
                    }
                }
            } else {
                BlockSupportRequiredComponent supportComponent = blockAtPosition.getEntity().getComponent(BlockSupportRequiredComponent.class);
                if (supportComponent != null) {
                    if (side.isHorizontal() && supportComponent.sideAllowed
                            && !hasSupport(blockPosition, supportComponent)) {
                        destroyBlock = true;
                    } else if (!side.isHorizontal() && (supportComponent.topAllowed || supportComponent.bottomAllowed)
                            && !hasSupport(blockPosition, supportComponent)) {
                        destroyBlock = true;
                    }
                }
            }
        }
        if (destroyBlock) {
            blockEntityRegistry.getBlockEntityAt(blockPosition).send(new DestroyEvent(gatheringEntity, EntityRef.NULL, prefabManager.getPrefab("engine:supportRemovedDamage")));
        }
    }

    private boolean hasSupport(Vector3i blockPosition, BlockSupportRequiredComponent supportComponent) {
        if (supportComponent.bottomAllowed && hasBlockOnSide(blockPosition, Side.BOTTOM)) {
            return true;
        }
        if (supportComponent.topAllowed && hasBlockOnSide(blockPosition, Side.TOP)) {
            return true;
        }
        if (supportComponent.sideAllowed && (hasBlockOnSide(blockPosition, Side.LEFT) || hasBlockOnSide(blockPosition, Side.RIGHT)
                || hasBlockOnSide(blockPosition, Side.FRONT) || hasBlockOnSide(blockPosition, Side.BACK))) {
            return true;
        }
        return false;
    }

    private boolean hasBlockOnSide(Vector3i blockPosition, Side side) {
        return worldProvider.getBlock(side.getAdjacentPos(blockPosition)) != BlockManager.getAir();
    }
}
