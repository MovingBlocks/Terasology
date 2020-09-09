// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseStrictlySparseFieldFacet2D;

import java.util.Optional;

/***
 * Stores the sea level, if it varies or is not defined for certain coordinates.
 */
public class StrictlySparseSeaLevelFacet extends BaseStrictlySparseFieldFacet2D {
    public StrictlySparseSeaLevelFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public Optional<Integer> getSeaLevel(int x, int y) {
        Optional<Float> seaLevel = getWorld(x, y);
        return seaLevel.map(TeraMath::floorToInt);
    }
}
