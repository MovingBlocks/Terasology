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
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
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
 */
@RegisterSystem
@Share(BlockStructuralSupportRegistry.class)
public class BlockStructuralSupportSystem extends BaseComponentSystem implements BlockStructuralSupportRegistry {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
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
    public void preBegin() {
        registerBlockStructuralSupport(new AttachSupportRequired());
        registerBlockStructuralSupport(new BlockDefSupportRequired());
        registerBlockStructuralSupport(new SideBlockSupportRequired());
    }

    @Override
    public void registerBlockStructuralSupport(BlockStructuralSupport blockStructuralSupport) {
        supports.add(blockStructuralSupport);
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void checkForSupportRemoved(OnChangedBlock event, EntityRef entity) {
        PerformanceMonitor.startActivity("StructuralCheck");
        try {
            for (Side side : Side.values()) {
                validateSupportForBlockOnSide(event.getBlockPosition(), side);
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
                    blockEntityRegistry.getBlockEntityAt(blockPosition).send(new DestroyEvent(gatheringEntity,
                            EntityRef.NULL, prefabManager.getPrefab("engine:supportRemovedDamage")));
                    break;
                }
            }
        }
    }
}
