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
package org.terasology.world.generation2.perlin;

import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3DTo2DAdapter;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise2D;
import org.terasology.world.generation2.FacetProvider;
import org.terasology.world.generation2.GeneratingRegion;
import org.terasology.world.generation2.Produces;
import org.terasology.world.generation2.Requires;
import org.terasology.world.generation2.facets.SurfaceHeightFacet;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
@Requires(SurfaceHeightFacet.class)
@Produces(SurfaceHeightFacet.class)
public class PerlinOceanProvider implements FacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise2D oceanNoise;

    @Override
    public void setSeed(long seed) {
        oceanNoise = new SubSampledNoise2D(new Noise3DTo2DAdapter(new BrownianNoise3D(new PerlinNoise(seed + 1), 8)), new Vector2f(0.0009f, 0.0009f), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region) {
        Region3i processRegion = region.getRegion();
        float[] noise = oceanNoise.noise(Rect2i.createFromMinAndSize(processRegion.minX(), processRegion.minZ(), processRegion.sizeX(), processRegion.sizeZ()));

        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        float[] surfaceHeights = facet.getInternal();
        for (int i = 0; i < noise.length; ++i) {
            surfaceHeights[i] *= TeraMath.clamp(noise[i] * 8.0f + 0.25f);
        }
    }
}
