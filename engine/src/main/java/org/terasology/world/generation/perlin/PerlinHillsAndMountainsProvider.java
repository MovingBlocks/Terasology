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
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3DTo2DAdapter;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise2D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.HumidityFacet;
import org.terasology.world.generation.facets.SeaLevelTemperatureFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
@Requires({SurfaceHeightFacet.class, SeaLevelTemperatureFacet.class, HumidityFacet.class})
@Produces(SurfaceHeightFacet.class)
public class PerlinHillsAndMountainsProvider implements FacetProvider {

    private SubSampledNoise2D mountainNoise;
    private SubSampledNoise2D hillNoise;

    @Override
    public void setSeed(long seed) {
        mountainNoise = new SubSampledNoise2D(new Noise3DTo2DAdapter(new BrownianNoise3D(new PerlinNoise(seed + 3))), new Vector2f(0.0002f, 0.0002f), 4);
        hillNoise = new SubSampledNoise2D(new Noise3DTo2DAdapter(new BrownianNoise3D(new PerlinNoise(seed + 4))), new Vector2f(0.0008f, 0.0008f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        Rect2i region2d = Rect2i.createFromMinAndMax(region.getRegion().minX(), region.getRegion().minZ(), region.getRegion().maxX(), region.getRegion().maxZ());

        float[] mountainData = mountainNoise.noise(region2d);
        float[] hillData = hillNoise.noise(region2d);
        float[] temperatureData = region.getRegionFacet(SeaLevelTemperatureFacet.class).getInternal();
        float[] humidityData = region.getRegionFacet(HumidityFacet.class).getInternal();

        float[] heightData = region.getRegionFacet(SurfaceHeightFacet.class).getInternal();
        for (int i = 0; i < mountainData.length; ++i) {
            float tempHumid = temperatureData[i] * humidityData[i];
            Vector2f distanceToMountainBiome = new Vector2f(temperatureData[i] - 0.25f, tempHumid - 0.35f);
            float mIntens = TeraMath.clamp(1.0f - distanceToMountainBiome.length() * 3.0f);
            float densityMountains = Math.max(mountainData[i], 0) * mIntens;
            float densityHills = Math.max(hillData[i] - 0.1f, 0) * (1.0f - mIntens);

            heightData[i] = heightData[i] + 1024 * densityMountains + 128 * densityHills;
        }
    }

}
