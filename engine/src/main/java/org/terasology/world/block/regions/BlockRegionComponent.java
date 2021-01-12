// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block.regions;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.world.block.BlockRegion;

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
