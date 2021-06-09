// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.regions;

import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;


public class BlockRegionComponent implements Component<BlockRegionComponent> {
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

    @Override
    public void copy(BlockRegionComponent other) {
        this.region = new BlockRegion(region);
    }
}
