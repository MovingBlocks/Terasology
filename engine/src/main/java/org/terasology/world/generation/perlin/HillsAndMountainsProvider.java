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

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.providers.HumidityProvider;
import org.terasology.world.generation.providers.SeaLevelTemperatureProvider;
import org.terasology.world.generation.providers.SurfaceHeightProvider;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class HillsAndMountainsProvider implements SurfaceHeightProvider {

    @Requires
    private SurfaceHeightProvider baseProvider;

    @Requires
    private HumidityProvider humidityProvider;

    @Requires
    private SeaLevelTemperatureProvider temperatureProvider;

    private BrownianNoise2D hillsNoise;
    private BrownianNoise2D mountainsNoise;

    @Override
    public float getHeightAt(float x, float z) {
        float temp = temperatureProvider.getTemperature(x, z);
        float humidity = humidityProvider.getHumidity(x, z) * temp;

        Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f, humidity - 0.35f);

        float mIntens = TeraMath.clamp(1.0f - distanceToMountainBiome.length() * 3.0f);
        float densityMountains = calcMountainDensity(x, z) * mIntens;
        float densityHills = calcHillDensity(x, z) * (1.0f - mIntens);
        return baseProvider.getHeightAt(x, z) + densityMountains * 1024.0f + densityHills * 128.0f;
    }

    private float calcMountainDensity(float x, float z) {
        float x1 = x * 0.00002f;
        float z1 = z * 0.00002f;

        float result = (float) mountainsNoise.noise(x1, z1);
        return result > 0.0f ? result : 0f;
    }

    private float calcHillDensity(float x, float z) {
        float x1 = x * 0.0008f;
        float z1 = z * 0.0008f;

        float result = (float) hillsNoise.noise(x1, z1) - 0.1f;
        return result > 0.0 ? result : 0;
    }

    @Override
    public void setSeed(long seed) {
        hillsNoise = new BrownianNoise2D(new SimplexNoise(seed + 3));
        mountainsNoise = new BrownianNoise2D(new SimplexNoise(seed + 4));
    }
}
