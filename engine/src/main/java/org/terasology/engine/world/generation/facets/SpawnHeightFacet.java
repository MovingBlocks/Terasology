// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseStrictlySparseFieldFacet2D;

/***
 * Stores the height at which the player may be spawned, if it exists for a given coordinate
 */
public class SpawnHeightFacet extends BaseStrictlySparseFieldFacet2D {
    public SpawnHeightFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
