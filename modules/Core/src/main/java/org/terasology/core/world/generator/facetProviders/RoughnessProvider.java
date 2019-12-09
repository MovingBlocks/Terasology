/*
 * Copyright 2019 MovingBlocks
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

import org.terasology.core.world.generator.facets.RoughnessFacet;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Produces(RoughnessFacet.class)
@Requires({@Facet(value = SurfaceHeightFacet.class),
        @Facet(value = SeaLevelFacet.class)})
/**
 * This facet will be used to store information about the height variations in grid cells with the size a
 * It will be saved as a Component in the RegionEntity
 */
public class RoughnessProvider implements FacetProvider {

    private final int gridSize = 4;


    @Override
    public void setSeed(long seed) { }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        RoughnessFacet facet = new RoughnessFacet(region.getRegion(), border, gridSize);

        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i processRegion = facet.getGridWorldRegion();

        for (BaseVector2i pos : processRegion.contents()) {

            if(surfaceHeightFacet.getWorld(pos) > seaLevelFacet.getSeaLevel()) {
                facet.calcRoughness(new Vector2i(pos.x(), pos.y()), surfaceHeightFacet);
            } else {
                facet.setWorld(pos, -1000);
            }
        }

        region.setRegionFacet(RoughnessFacet.class, facet);
    }


}
