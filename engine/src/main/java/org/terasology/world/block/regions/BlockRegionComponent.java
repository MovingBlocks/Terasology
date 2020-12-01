// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block.regions;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.world.block.BlockRegion;

/**
 */
public class BlockRegionComponent implements Component {
    @Replicate
    public BlockRegion region = new BlockRegion();
    public boolean overrideBlockEntities = true;

    public BlockRegionComponent() {
    }

    public BlockRegionComponent(BlockRegion region) {
        this.region.set(region);
    }
}
