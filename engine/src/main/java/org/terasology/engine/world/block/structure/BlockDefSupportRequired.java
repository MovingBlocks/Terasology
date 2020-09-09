// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.structure;

import org.terasology.engine.math.Side;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

import java.util.Collections;
import java.util.Map;

public class BlockDefSupportRequired implements BlockStructuralSupport {
    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isSufficientlySupported(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final Block block = getBlockWithOverrides(location, blockOverrides);
        if (block.isSupportRequired()) {
            final Vector3i bottomLocation = Side.BOTTOM.getAdjacentPos(location);
            return !getWorldProvider().isBlockRelevant(bottomLocation)
                    || getBlockWithOverrides(bottomLocation, blockOverrides).isFullSide(Side.TOP);
        }
        return true;
    }

    @Override
    public boolean shouldBeRemovedDueToChange(Vector3i location, Side sideChanged) {
        return sideChanged == Side.BOTTOM && !isSufficientlySupported(location,
                Collections.emptyMap());
    }

    private Block getBlockWithOverrides(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final Block blockFromOverride = blockOverrides.get(location);
        if (blockFromOverride != null) {
            return blockFromOverride;
        }
        return getWorldProvider().getBlock(location);
    }

    private WorldProvider getWorldProvider() {
        return CoreRegistry.get(WorldProvider.class);
    }
}
