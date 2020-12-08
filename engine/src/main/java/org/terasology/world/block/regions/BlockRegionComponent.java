// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block.regions;

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.network.Replicate;

/**
 */
public class BlockRegionComponent implements Component {
    @Replicate
    public Region3i region = Region3i.empty();
    public boolean overrideBlockEntities = true;

    public BlockRegionComponent() {
    }

    public BlockRegionComponent(Region3i region) {
        this.region = region;
    }
}
