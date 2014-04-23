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

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise3D;
import org.terasology.world.generation2.FacetProvider;
import org.terasology.world.generation2.GeneratingRegion;
import org.terasology.world.generation2.Produces;
import org.terasology.world.generation2.Requires;
import org.terasology.world.generation2.facets.Boolean3DIterator;
import org.terasology.world.generation2.facets.HumidityFacet;
import org.terasology.world.generation2.facets.SeaLevelTemperatureFacet;
import org.terasology.world.generation2.facets.SolidityFacet;
import org.terasology.world.generation2.facets.SurfaceHeightFacet;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@Requires({SurfaceHeightFacet.class, SeaLevelTemperatureFacet.class, HumidityFacet.class})
@Produces(SolidityFacet.class)
public class PerlinHillsAndMountainsProvider implements FacetProvider {

    private SubSampledNoise3D mountainNoise;
    private SubSampledNoise3D hillNoise;

    @Override
    public void setSeed(long seed) {
        mountainNoise = new SubSampledNoise3D(new BrownianNoise3D(new PerlinNoise(seed + 3)), new Vector3f(0.002f, 0.001f, 0.002f), 4);
        hillNoise = new SubSampledNoise3D(new BrownianNoise3D(new PerlinNoise(seed + 4)), new Vector3f(0.008f, 0.006f, 0.008f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        float[] height = region.getRegionFacet(SurfaceHeightFacet.class).getInternal();
        float[] mountainData = mountainNoise.noise(region.getRegion());
        float[] hillData = hillNoise.noise(region.getRegion());
        float[] temperatureData = region.getRegionFacet(SeaLevelTemperatureFacet.class).getInternal();
        float[] humidityData = region.getRegionFacet(HumidityFacet.class).getInternal();

        Vector3i offset = region.getRegion().min();

        SolidityFacet result = new SolidityFacet(region.getRegion().size());
        Boolean3DIterator iterator = result.get();
        int i = 0;
        while (iterator.hasNext()) {
            iterator.next();
            int index2d = iterator.currentPosition().getX() + region.getRegion().sizeX() * iterator.currentPosition().getZ();
            iterator.setLast(isSolid(iterator.currentPosition().getY() + offset.getY(), height[index2d], mountainData[i], hillData[i],
                    temperatureData[index2d], humidityData[index2d]));
            i++;
        }
        region.setRegionFacet(SolidityFacet.class, result);
    }

    private boolean isSolid(int y, float height, float mountain, float hill, float temperature, float humidity) {
        float tempHumid = temperature * humidity;

        Vector2f distanceToMountainBiome = new Vector2f(temperature - 0.25f, tempHumid - 0.35f);

        float mIntens = TeraMath.clamp(1.0f - distanceToMountainBiome.length() * 3.0f);
        float densityMountains = Math.max(mountain, 0) * mIntens;
        float densityHills = Math.max(hill - 0.1f, 0) * (1.0f - mIntens);
        return -y + height + 1024 * densityMountains + 128 * densityHills > 0;
    }

}
