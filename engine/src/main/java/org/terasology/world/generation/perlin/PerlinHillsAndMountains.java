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
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.providers.BatchSurfaceHeightProvider;
import org.terasology.world.generation.providers.HumidityProvider;
import org.terasology.world.generation.providers.SeaLevelTemperatureProvider;
import org.terasology.world.generation.providers.SolidityProvider;
import org.terasology.world.generation.providers.SurfaceHeightProvider;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class PerlinHillsAndMountains implements SolidityProvider {

    @Requires
    private BatchSurfaceHeightProvider baseHeightProvider;

    @Requires
    private SeaLevelTemperatureProvider temperatureProvider;

    @Requires
    private HumidityProvider humidityProvider;

    private Noise3D mountainNoise;
    private Noise3D hillNoise;

    @Override
    public void setSeed(long seed) {
        mountainNoise = new BrownianNoise3D(new PerlinNoise(seed + 3));
        hillNoise = new BrownianNoise3D(new PerlinNoise(seed + 4));
    }

    private float calcMountainDensity(float x, float y, float z) {
        float x1 = x * 0.002f;
        float y1 = y * 0.001f;
        float z1 = z * 0.002f;

        float result = (float) mountainNoise.noise(x1, y1, z1);
        return result > 0.0f ? result : 0;
    }

    private float calcHillDensity(float x, float y, float z) {
        float x1 = x * 0.008f;
        float y1 = y * 0.006f;
        float z1 = z * 0.008f;

        float result = (float) hillNoise.noise(x1, y1, z1) - 0.1f;
        return result > 0.0f ? result : 0;
    }

    @Override
    public boolean[] isSolid(Region3i region) {
        boolean[] results = new boolean[region.sizeX() * region.sizeY() * region.sizeZ()];
        float[] heights = baseHeightProvider.getSurfaceHeights(Rect2i.createFromMinAndSize(region.minX(), region.minZ(), region.sizeX(), region.sizeZ()));
        for (int z = 0; z < region.sizeZ(); ++z) {
            for (int x = 0; x < region.sizeX(); ++x) {
                float height = heights[x + region.sizeX() * z];
                for (int y = 0; y < region.sizeY(); ++y) {
                    results[x + region.sizeX() * (y + region.sizeY() * z)] = isSolid(x + region.minX(), y + region.minY(), z + region.minZ(), height);
                }
            }
        }
        return results;
    }

    private boolean isSolid(float x, float y, float z, float height) {
        float temp = temperatureProvider.getTemperature(x, z);
        float humidity = humidityProvider.getHumidity(x, z) * temp;

        Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f, humidity - 0.35f);

        float mIntens = TeraMath.clamp(1.0f - distanceToMountainBiome.length() * 3.0f);
        float densityMountains = calcMountainDensity(x, y, z) * mIntens;
        float densityHills = calcHillDensity(x, y, z) * (1.0f - mIntens);
        return -y + height + 1024 * densityMountains + 128 * densityHills > 0;
    }
}
