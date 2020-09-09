// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetName;
import org.terasology.engine.world.generation.facets.base.BaseFieldFacet2D;

/**
 */
@FacetName("Temperature")
public class SurfaceTemperatureFacet extends BaseFieldFacet2D {
    static int maxSamplesPerRegion = 5;

    public SurfaceTemperatureFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
