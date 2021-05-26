// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.world.generation.facets.base.BaseFieldFacet3D;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;

public class DensityFacet extends BaseFieldFacet3D {

    public DensityFacet(BlockRegionc targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
