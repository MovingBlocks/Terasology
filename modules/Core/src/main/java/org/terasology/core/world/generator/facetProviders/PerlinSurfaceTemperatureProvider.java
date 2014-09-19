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

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3DTo2DAdapter;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise2D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceTemperatureFacet;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
@Produces(SurfaceTemperatureFacet.class)
public class PerlinSurfaceTemperatureProvider implements FacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise2D temperatureNoise;

    @Override
    public void setSeed(long seed) {
        temperatureNoise = new SubSampledNoise2D(new Noise3DTo2DAdapter(new BrownianNoise3D(new PerlinNoise(seed + 5), 8)), new Vector2f(0.0005f, 0.0005f), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceTemperatureFacet facet = new SurfaceTemperatureFacet(region.getRegion(), region.getBorderForFacet(SurfaceTemperatureFacet.class));
        float[] noise = this.temperatureNoise.noise(facet.getWorldRegion());

        for (int i = 0; i < noise.length; ++i) {
            noise[i] = TeraMath.clamp((noise[i] + 1f) * 0.5f);
        }

        facet.set(noise);
        region.setRegionFacet(SurfaceTemperatureFacet.class, facet);
    }
}
