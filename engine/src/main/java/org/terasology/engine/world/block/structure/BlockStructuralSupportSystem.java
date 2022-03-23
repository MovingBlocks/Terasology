// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.structure;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DestroyEvent;
import org.terasology.engine.math.Side;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

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

    @ReceiveEvent(components = BlockComponent.class)
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
        final Map<Vector3ic, Block> blocksMap = placeBlocks.getBlocks();
        for (BlockStructuralSupport support : supports) {
            for (Map.Entry<Vector3ic, Block> blockEntry : blocksMap.entrySet()) {
                final Vector3ic position = blockEntry.getKey();
                if (!support.isSufficientlySupported(position, Collections.unmodifiableMap(blocksMap))) {
                    placeBlocks.consume();
                    return;
                }
            }
        }
    }

    private void validateSupportForBlockOnSide(Vector3i replacedBlockPosition, Side side) {
        final Vector3i blockPosition = side.getAdjacentPos(replacedBlockPosition, new Vector3i());
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
