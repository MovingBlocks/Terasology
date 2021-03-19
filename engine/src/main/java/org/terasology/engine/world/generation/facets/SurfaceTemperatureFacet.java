// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetName;

/**
 */
@FacetName("Temperature")
public class SurfaceTemperatureFacet extends BaseFieldFacet2D {
    static int maxSamplesPerRegion = 5;

    public SurfaceTemperatureFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
