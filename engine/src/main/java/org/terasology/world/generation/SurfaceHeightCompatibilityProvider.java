// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.world.generation.facets.SurfacesFacet;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Converts from ElevationFacet and SurfacesFacet to SurfaceHeightFacet,
 * to be used for backwards compatibility while transitioning away from SurfaceHeightFacet.
 *
 * The output heights may in some cases depend on the vertical extent of the
 * generating region, but usually not in a way that substantially affects generation.
 */
@Produces(SurfaceHeightFacet.class)
@Requires({@Facet(ElevationFacet.class), @Facet(SurfacesFacet.class)})
public class SurfaceHeightCompatibilityProvider implements FacetProvider {
    @Override
    public void process(GeneratingRegion region) {
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        ElevationFacet elevationFacet = region.getRegionFacet(ElevationFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), region.getBorderForFacet(SurfaceHeightFacet.class));

        for (BaseVector2i pos : facet.getWorldRegion().contents()) {
            facet.setWorld(pos, surfacesFacet.getPrimarySurface(elevationFacet, pos.x(), pos.y()).orElse(elevationFacet.getWorld(pos)));
        }
        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }
}
