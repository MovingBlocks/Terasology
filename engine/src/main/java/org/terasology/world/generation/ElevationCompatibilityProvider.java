// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation;

import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Converts from SurfaceHeightFacet to ElevationFacet,
 * to be used for backwards compatibility while transitioning away from SurfaceHeightFacet.
 */
@Produces(ElevationFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
public class ElevationCompatibilityProvider implements FacetProvider {
    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surfaceHeight = region.getRegionFacet(SurfaceHeightFacet.class);
        ElevationFacet facet = new ElevationFacet(region.getRegion(), region.getBorderForFacet(ElevationFacet.class));

        for (BaseVector2i pos : facet.getRelativeRegion().contents()) {
            facet.set(pos, surfaceHeight.get(pos));
        }
        region.setRegionFacet(ElevationFacet.class, facet);
    }
}
