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
package org.terasology.world.block.structure;

import com.google.common.collect.Sets;
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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.placement.PlaceBlocks;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = {BlockStructuralSupportRegistry.class})
public class BlockStructuralSupportSystem extends BaseComponentSystem implements BlockStructuralSupportRegistry {
    public static final int GATHERING_INVENTORY_SLOT_COUNT = 20;
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

    private Set<BlockStructuralSupport> supports = Sets.newTreeSet(
            new Comparator<BlockStructuralSupport>() {
                @Override
                public int compare(BlockStructuralSupport o1, BlockStructuralSupport o2) {
                    return o1.getPriority() - o2.getPriority();
                }
            });

    @Override
    public void registerBlockStructuralSupport(BlockStructuralSupport blockStructuralSupport) {
        supports.add(blockStructuralSupport);
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void checkForSupportRemoved(OnChangedBlock event, EntityRef entity) {
        PerformanceMonitor.startActivity("StructuralCheck");
        try {
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
        } finally {
            PerformanceMonitor.endActivity();
        }
    }

    @ReceiveEvent
    public void preventInvalidPlacement(PlaceBlocks placeBlocks, EntityRef world) {
        final Map<Vector3i, Block> blocksMap = placeBlocks.getBlocks();
        for (BlockStructuralSupport support : supports) {
            for (Map.Entry<Vector3i, Block> blockEntry : blocksMap.entrySet()) {
                final Vector3i position = blockEntry.getKey();
                if (!support.isSufficientlySupported(position, Collections.unmodifiableMap(blocksMap))) {
                    placeBlocks.consume();
                    return;
                }
            }
        }
    }

    private void validateSupportForBlockOnSide(Vector3i replacedBlockPosition, Side side) {
        final Vector3i blockPosition = side.getAdjacentPos(replacedBlockPosition);
        if (worldProvider.isBlockRelevant(blockPosition)) {
            final Side sideReverse = side.reverse();

            for (BlockStructuralSupport support : supports) {
                if (support.shouldBeRemovedDueToChange(blockPosition, sideReverse)) {
                    System.out.println("Removing block due to: " + support.getClass());
                    blockEntityRegistry.getBlockEntityAt(blockPosition).send(new DestroyEvent(gatheringEntity, EntityRef.NULL, prefabManager.getPrefab("engine:supportRemovedDamage")));
                    break;
                }
            }
        }
    }
}
