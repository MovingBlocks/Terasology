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
package org.terasology.world.generation.perlin;

import org.terasology.math.Region3i;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.Float2DIterator;

/**
 * @author Immortius
 */
@Requires(SurfaceHeightFacet.class)
@Produces(DensityFacet.class)
public class PerlinSurfaceToDensityProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {

    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surfaceHeight = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet facet = new DensityFacet(region.getRegion().size());
        float[] result = facet.getInternal();
        Float2DIterator iterator = surfaceHeight.get();
        Region3i area = region.getRegion();
        while (iterator.hasNext()) {
            float height = iterator.next();
            for (int y = 0; y < region.getRegion().sizeY(); ++y) {
                int index = iterator.currentPosition().x + area.sizeX() * (y + area.sizeY() * iterator.currentPosition().getY());
                result[index] = height - area.minY() - y;
            }
        }
        region.setRegionFacet(DensityFacet.class, facet);
    }
}
