// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.regions;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.Replicate;

/**
 *
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
