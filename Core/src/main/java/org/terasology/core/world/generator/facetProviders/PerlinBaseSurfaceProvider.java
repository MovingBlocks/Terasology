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

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 */
@Produces(SurfaceHeightFacet.class)
@Requires(@Facet(SeaLevelFacet.class))
public class PerlinBaseSurfaceProvider implements FacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise surfaceNoise;

    @Override
    public void setSeed(long seed) {
        BrownianNoise source = new BrownianNoise(new PerlinNoise(seed), 8);
        surfaceNoise = new SubSampledNoise(source, new Vector2f(0.004f, 0.004f), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        float seaLevel = seaLevelFacet.getSeaLevel();
        Rect2i processRegion = facet.getWorldRegion();
        float[] noise = surfaceNoise.noise(processRegion);

        for (int i = 0; i < noise.length; ++i) {
            noise[i] = seaLevel + seaLevel * ((noise[i] * 2.11f + 1f) / 2f);
        }

        facet.set(noise);
        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }
}
