// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

/**
 * Stores the surface depth limits.
 * The surface depth limit (inclusive) is an optional bottom limit for the default world generator.
 * No blocks below the surface depth limit are altered..
 */
public class SurfaceDepthFacet extends BaseFieldFacet2D {

    public SurfaceDepthFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
