// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity.neighbourUpdate;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.UpdatesWithNeighboursFamily;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.items.OnBlockItemPlaced;

import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class NeighbourBlockFamilyUpdateSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(NeighbourBlockFamilyUpdateSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private int largeBlockUpdateCount;
    private Set<Vector3i> blocksUpdatedInLargeBlockUpdate = Sets.newHashSet();

    @ReceiveEvent
    public void largeBlockUpdateStarting(LargeBlockUpdateStarting event, EntityRef entity) {
        largeBlockUpdateCount++;
    }

    @ReceiveEvent
    public void largeBlockUpdateFinished(LargeBlockUpdateFinished event, EntityRef entity) {
        largeBlockUpdateCount--;
        if (largeBlockUpdateCount < 0) {
            largeBlockUpdateCount = 0;
            throw new IllegalStateException("LargeBlockUpdateFinished invoked too many times");
        }

        if (largeBlockUpdateCount == 0) {
            notifyNeighboursOfChangedBlocks();
        }
    }

    /**
     * Notifies the adjacent block families when a block is placed next to them.
     * @param event
     * @param entity
     */
    @ReceiveEvent
    public void onBlockPlaced(OnBlockItemPlaced event, EntityRef entity) {
        BlockComponent blockComponent = event.getPlacedBlock().getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }

        processUpdateForBlockLocation(blockComponent.getPosition());
    }

    private void notifyNeighboursOfChangedBlocks() {
        // Invoke the updates in another large block change for this class only
        largeBlockUpdateCount++;
        while (!blocksUpdatedInLargeBlockUpdate.isEmpty()) {
            Set<Vector3i> blocksToUpdate = blocksUpdatedInLargeBlockUpdate;

            // Setup new collection for blocks changed in this pass
            blocksUpdatedInLargeBlockUpdate = Sets.newHashSet();

            blocksToUpdate.forEach(this::processUpdateForBlockLocation);
        }
        largeBlockUpdateCount--;
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockUpdate(OnChangedBlock event, EntityRef blockEntity) {
        if (largeBlockUpdateCount > 0) {
            blocksUpdatedInLargeBlockUpdate.add(event.getBlockPosition());
        } else {
            Vector3i blockLocation = event.getBlockPosition();
            processUpdateForBlockLocation(blockLocation);
        }
    }

    private void processUpdateForBlockLocation(Vector3ic blockLocation) {
        for (Side side : Side.getAllSides()) {
            Vector3i neighborLocation = blockLocation.add(side.direction(), new Vector3i());
            if (worldProvider.isBlockRelevant(neighborLocation)) {
                Block neighborBlock = worldProvider.getBlock(neighborLocation);
                final BlockFamily blockFamily = neighborBlock.getBlockFamily();
                if (blockFamily instanceof UpdatesWithNeighboursFamily) {
                    UpdatesWithNeighboursFamily neighboursFamily = (UpdatesWithNeighboursFamily) blockFamily;
                    Block neighborBlockAfterUpdate = neighboursFamily.getBlockForNeighborUpdate(neighborLocation, neighborBlock);
                    if (neighborBlock != neighborBlockAfterUpdate) {
                        worldProvider.setBlock(neighborLocation, neighborBlockAfterUpdate);
                    }
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        if (largeBlockUpdateCount > 0) {
            logger.error("Unmatched LargeBlockUpdateStarted - LargeBlockUpdateFinished not invoked enough times");
        }
        largeBlockUpdateCount = 0;
    }
}
