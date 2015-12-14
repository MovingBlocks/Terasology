/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.world.generator.facetProviders;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Sets density based on its distance from the surface
 */
@Requires(@Facet(SurfaceHeightFacet.class))
@Produces(DensityFacet.class)
public class SurfaceToDensityProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {

    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surfaceHeight = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet facet = new DensityFacet(region.getRegion(), region.getBorderForFacet(DensityFacet.class));

        Region3i area = region.getRegion();
        Rect2i rect = Rect2i.createFromMinAndMax(facet.getRelativeRegion().minX(), facet.getRelativeRegion().minZ(),
                facet.getRelativeRegion().maxX(), facet.getRelativeRegion().maxZ());
        for (BaseVector2i pos : rect.contents()) {
            float height = surfaceHeight.get(pos);
            for (int y = facet.getRelativeRegion().minY(); y <= facet.getRelativeRegion().maxY(); ++y) {
                facet.set(pos.x(), y, pos.y(), height - area.minY() - y);
            }
        }
        region.setRegionFacet(DensityFacet.class, facet);
    }
}
