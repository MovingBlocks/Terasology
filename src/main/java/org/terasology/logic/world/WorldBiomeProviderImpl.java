/*
 * Copyright 2012
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

package org.terasology.logic.world;

import org.terasology.math.TeraMath;
import org.terasology.utilities.PerlinNoise;

/**
 * @author Immortius
 */
// TODO: Evolve this class into a world description provider (density, height, etc) to feed into the world generators
public class WorldBiomeProviderImpl implements WorldBiomeProvider {

    PerlinNoise temperatureNoise, humidityNoise, fogNoise;

    public WorldBiomeProviderImpl(String worldSeed) {
        temperatureNoise = new PerlinNoise(worldSeed.hashCode() + 5);
        humidityNoise = new PerlinNoise(worldSeed.hashCode() + 6);
        fogNoise = new PerlinNoise(worldSeed.hashCode());
    }

    @Override
    public float getHumidityAt(int x, int z) {
        double result = humidityNoise.fBm(x * 0.0005, 0, 0.0005 * z);
        return (float) TeraMath.clamp((result + 1.0f) / 2.0f);
    }

    @Override
    public float getTemperatureAt(int x, int z) {
        double result = temperatureNoise.fBm(x * 0.0005, 0, 0.0005 * z);
        return (float) TeraMath.clamp((result + 1.0f) / 2.0f);
    }

    @Override
    public float getFog(float time, float daylight) {
        return (float) TeraMath.clamp(fogNoise.noise(time * 2.0, 0.038291, 0.874691) * 2.0 * daylight + 0.1 * daylight, 0.0, 8.0) + 0.05f;
    }

    @Override
    public Biome getBiomeAt(int x, int z) {
        double temp = getTemperatureAt(x, z);
        double humidity = getHumidityAt(x, z);

        if (temp >= 0.5 && humidity < 0.3) {
            return Biome.DESERT;
        } else if (humidity >= 0.3 && humidity <= 0.6 && temp >= 0.5) {
            return Biome.PLAINS;
        } else if (temp <= 0.3 && humidity > 0.5) {
            return Biome.SNOW;
        } else if (humidity >= 0.2 && humidity <= 0.6 && temp < 0.5) {
            return Biome.MOUNTAINS;
        }

        return Biome.FOREST;
    }

    @Override
    public Biome getBiomeAt(float x, float z) {
        return getBiomeAt(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }
}
