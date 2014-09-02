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

import org.terasology.core.world.generator.facets.World2dPreviewFacet;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Produces(World2dPreviewFacet.class)
@Requires({@Facet(SurfaceHeightFacet.class), @Facet(SeaLevelFacet.class)})
public class World2dPreviewProvider implements FacetProvider {
    static int maxSamplesPerRegion = 4;
    static int maxDepthDescribed = 64;
    static int maxHeightDescribed = 100;

    @Override
    public void setSeed(long seed) {

    }

    @Override
    public void process(GeneratingRegion region) {
        World2dPreviewFacet facet = new World2dPreviewFacet();
        SurfaceHeightFacet surfaceFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        float seaLevel = seaLevelFacet.getSeaLevel();

        float[] values = surfaceFacet.getInternal();
        float total = 0;
        // averaging every single value takes too much time, only use some of the values in our average
        int sampleRate = Math.max(1, region.getRegion().sizeX() / maxSamplesPerRegion);
        for (int i = 0; i < values.length; i++) {
            if (i % sampleRate == 0) {
                total += values[i];
            }
        }
        float average = total / (values.length / sampleRate);

        if (average < seaLevel) {
            // start with blue and make closer to black the deeper we go
            facet.setColor(new Color(0, 0, TeraMath.clamp((int) (255 * (1.0 - (Math.abs(average - seaLevel) / maxDepthDescribed))), 100, 255)));
        } else {
            // start with green make closer to black the higher we go
            facet.setColor(new Color(0, TeraMath.clamp((int) (255 * (1.0 - ((average - seaLevel) / maxHeightDescribed))), 100, 255), 0));
        }

        region.setRegionFacet(World2dPreviewFacet.class, facet);
    }
}
