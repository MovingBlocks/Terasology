// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.structure;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockPart;
import org.terasology.engine.world.block.shapes.BlockMeshPart;

import java.util.Collections;
import java.util.Map;

public class AttachSupportRequired implements BlockStructuralSupport {
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean shouldBeRemovedDueToChange(Vector3i location, Side sideChanged) {
        final AttachSupportRequiredComponent component = getComponent(location, Collections.emptyMap());
        if (component != null) {
            final Block block = getBlockWithOverrides(location, Collections.emptyMap());
            if (!hasRequiredSupportOnSideForBlock(location, sideChanged, block)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRequiredSupportOnSideForBlock(Vector3ic location, Side sideChanged, Block block) {
        final BlockMeshPart part = block.getPrimaryAppearance().getPart(BlockPart.fromSide(sideChanged));
        // This block has mesh on this side, therefore it requires a support on that side
        return part == null || hasSupportFromBlockOnSide(location, sideChanged, Collections.emptyMap());
    }

    @Override
    public boolean isSufficientlySupported(Vector3ic location, Map<? extends Vector3ic, Block> blockOverrides) {
        final AttachSupportRequiredComponent component = getComponent(location, blockOverrides);
        if (component != null) {
            final Block block = getBlockWithOverrides(location, blockOverrides);
            for (Side side : Side.values()) {
                if (hasRequiredSupportOnSideForBlock(location, side, block)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private EntityRef getEntity(Vector3ic location, Map<? extends Vector3ic, Block> blockOverrides) {
        final Block overwrittenBlock = blockOverrides.get(location);
        if (overwrittenBlock != null) {
            return overwrittenBlock.getEntity();
        }
        EntityRef blockEntity = getBlockEntityRegistry().getExistingBlockEntityAt(location);
        if (blockEntity.exists()) {
            return blockEntity;
        } else {
            return getWorldProvider().getBlock(location).getEntity();
        }
    }

    private AttachSupportRequiredComponent getComponent(Vector3ic location, Map<? extends Vector3ic, Block> blockOverrides) {
        return getEntity(location, blockOverrides).getComponent(AttachSupportRequiredComponent.class);
    }

    private boolean hasSupportFromBlockOnSide(Vector3ic blockPosition, Side side, Map<? extends Vector3ic, Block> blockOverrides) {
        final Vector3i sideBlockPosition = side.getAdjacentPos(blockPosition, new Vector3i());
        if (!getWorldProvider().isBlockRelevant(sideBlockPosition)) {
            return true;
        }
        return getBlockWithOverrides(sideBlockPosition, blockOverrides).canAttachTo(side.reverse());
    }

    private Block getBlockWithOverrides(Vector3ic location, Map<? extends Vector3ic, Block> blockOverrides) {
        final Block blockFromOverride = blockOverrides.get(location);
        if (blockFromOverride != null) {
            return blockFromOverride;
        }
        return getWorldProvider().getBlock(location);
    }

    private BlockEntityRegistry getBlockEntityRegistry() {
        return CoreRegistry.get(BlockEntityRegistry.class);
    }

    private WorldProvider getWorldProvider() {
        return CoreRegistry.get(WorldProvider.class);
    }
}
