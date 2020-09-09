// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFacet2D;

/**
 * Stores where sea level is
 */
public class SeaLevelFacet extends BaseFacet2D {

    int seaLevel;

    public SeaLevelFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

}
