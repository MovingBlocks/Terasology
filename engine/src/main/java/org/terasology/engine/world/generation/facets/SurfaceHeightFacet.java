// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFieldFacet2D;

/**
 */
public class SurfaceHeightFacet extends BaseFieldFacet2D {

    public SurfaceHeightFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
