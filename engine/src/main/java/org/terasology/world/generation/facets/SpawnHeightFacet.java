// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.world.generation.facets.base.BaseStrictlySparseFieldFacet2D;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

/***
 * Stores the height at which the player may be spawned, if it exists for a given coordinate
 */
public class SpawnHeightFacet extends BaseStrictlySparseFieldFacet2D {
    public SpawnHeightFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
