// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.regions;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;

/**
 *
 */
public class BlockRegionComponent implements Component {
    /**
     * May be null.
     */
    @Replicate
    public BlockRegion region;

    public BlockRegionComponent() {
    }

    public BlockRegionComponent(BlockRegion region) {
        this.region = new BlockRegion(region);
    }
}
