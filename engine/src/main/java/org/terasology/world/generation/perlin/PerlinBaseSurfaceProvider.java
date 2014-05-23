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

import org.terasology.math.Rect2i;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3DTo2DAdapter;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise2D;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
@Produces(SurfaceHeightFacet.class)
public class PerlinBaseSurfaceProvider implements FacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise2D surfaceNoise;

    @Override
    public void setSeed(long seed) {
        surfaceNoise = new SubSampledNoise2D(new Noise3DTo2DAdapter(new BrownianNoise3D(new PerlinNoise(seed), 8)), new Vector2f(0.004f, 0.004f), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);
        Rect2i processRegion = facet.getWorldRegion();
        float[] noise = surfaceNoise.noise(processRegion);

        for (int i = 0; i < noise.length; ++i) {
            noise[i] = 32f + 32f * ((noise[i] + 1f) / 2f);
        }

        facet.set(noise);
        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }
}
