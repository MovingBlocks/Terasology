// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.worlds;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

@Produces({SurfacesFacet.class, ElevationFacet.class})
public class FlatSurfaceHeightProvider implements FacetProvider {
    private int height;

    public FlatSurfaceHeightProvider(int height) {
        this.height = height;
    }

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet elevationFacet = new ElevationFacet(region.getRegion(), region.getBorderForFacet(ElevationFacet.class));
        SurfacesFacet surfacesFacet = new SurfacesFacet(region.getRegion(), region.getBorderForFacet(SurfacesFacet.class));

        for (Vector2ic pos : elevationFacet.getRelativeArea()) {
            elevationFacet.set(pos, height);
        }

        if (surfacesFacet.getWorldRegion().minY() <= height && height <= surfacesFacet.getWorldRegion().maxY()) {
            for (int x = surfacesFacet.getWorldRegion().minX(); x <= surfacesFacet.getWorldRegion().maxX(); x++) {
                for (int z = surfacesFacet.getWorldRegion().minZ(); z <= surfacesFacet.getWorldRegion().maxZ(); z++) {
                    surfacesFacet.setWorld(x, height, z, true);
                }
            }
        }

        region.setRegionFacet(ElevationFacet.class, elevationFacet);
        region.setRegionFacet(SurfacesFacet.class, surfacesFacet);
    }
}

