// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.world.generation.facets.base.BaseFacet2D;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

/**
 * Stores where sea level is
 */
public class SeaLevelFacet extends BaseFacet2D {

    int seaLevel;

    public SeaLevelFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

}
