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
package org.terasology.world.block;

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
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.AttachedToSurfaceFamily;

import java.util.Collections;
import java.util.Map;

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

    @ReceiveEvent
    public void preventInvalidPlacement(PlaceBlocks placeBlocks, EntityRef world) {
        final Map<Vector3i, Block> blocksMap = placeBlocks.getBlocks();
        for (Map.Entry<Vector3i, Block> blockEntry : blocksMap.entrySet()) {
            final Vector3i position = blockEntry.getKey();
            final Block block = blockEntry.getValue();
            if (block.isSupportRequired()) {
                final Vector3i bottomPos = Side.BOTTOM.getAdjacentPos(position);
                final Block setBottomBlock = blocksMap.get(bottomPos);
                if (setBottomBlock != null && setBottomBlock == BlockManager.getAir()) {
                    placeBlocks.consume();
                    break;
                } else if (setBottomBlock == null) {
                    final Block bottomBlockInWorld = worldProvider.getBlock(bottomPos);
                    if (bottomBlockInWorld == BlockManager.getAir()) {
                        placeBlocks.consume();
                        break;
                    }
                }
            }
            final BlockSupportRequiredComponent supportComponent = block.getEntity().getComponent(BlockSupportRequiredComponent.class);
            if (supportComponent != null) {
                if (!hasSupport(position, supportComponent, blocksMap)) {
                    placeBlocks.consume();
                    break;
                }
            }
        }

    }

    private void validateSupportForBlockOnSide(Vector3i replacedBlockPosition, Side side) {
        final Vector3i blockPosition = side.getAdjacentPos(replacedBlockPosition);
        boolean destroyBlock = false;
        if (worldProvider.isBlockRelevant(blockPosition)) {
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
                                && !hasSupport(blockPosition, supportComponent, Collections.<Vector3i, Block>emptyMap())) {
                            destroyBlock = true;
                        } else if (!side.isHorizontal() && (supportComponent.topAllowed || supportComponent.bottomAllowed)
                                && !hasSupport(blockPosition, supportComponent, Collections.<Vector3i, Block>emptyMap())) {
                            destroyBlock = true;
                        }
                    }
                } else {
                    BlockSupportRequiredComponent supportComponent = blockAtPosition.getEntity().getComponent(BlockSupportRequiredComponent.class);
                    if (supportComponent != null) {
                        if (side.isHorizontal() && supportComponent.sideAllowed
                                && !hasSupport(blockPosition, supportComponent, Collections.<Vector3i, Block>emptyMap())) {
                            destroyBlock = true;
                        } else if (!side.isHorizontal() && (supportComponent.topAllowed || supportComponent.bottomAllowed)
                                && !hasSupport(blockPosition, supportComponent, Collections.<Vector3i, Block>emptyMap())) {
                            destroyBlock = true;
                        }
                    }
                }
            }
            if (destroyBlock) {
                blockEntityRegistry.getBlockEntityAt(blockPosition).send(new DestroyEvent(gatheringEntity, EntityRef.NULL, prefabManager.getPrefab("engine:supportRemovedDamage")));
            }
        }
    }

    private boolean hasSupport(Vector3i blockPosition, BlockSupportRequiredComponent supportComponent, Map<Vector3i, Block> blockOverrides) {
        if (supportComponent.bottomAllowed && hasBlockOnSide(blockPosition, Side.BOTTOM, blockOverrides)) {
            return true;
        }
        if (supportComponent.topAllowed && hasBlockOnSide(blockPosition, Side.TOP, blockOverrides)) {
            return true;
        }
        if (supportComponent.sideAllowed && (hasBlockOnSide(blockPosition, Side.LEFT, blockOverrides) || hasBlockOnSide(blockPosition, Side.RIGHT, blockOverrides)
                || hasBlockOnSide(blockPosition, Side.FRONT, blockOverrides) || hasBlockOnSide(blockPosition, Side.BACK, blockOverrides))) {
            return true;
        }
        return false;
    }

    private boolean hasBlockOnSide(Vector3i blockPosition, Side side, Map<Vector3i, Block> blockOverrides) {
        final Vector3i sideBlockPosition = side.getAdjacentPos(blockPosition);
        if (!worldProvider.isBlockRelevant(sideBlockPosition)) {
            return true;
        }
        final Block overrideBlock = blockOverrides.get(sideBlockPosition);
        if (overrideBlock != null) {
            return overrideBlock != BlockManager.getAir();
        }
        return worldProvider.getBlock(sideBlockPosition) != BlockManager.getAir();
    }
}
