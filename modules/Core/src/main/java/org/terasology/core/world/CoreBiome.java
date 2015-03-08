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
package org.terasology.core.world;

import org.terasology.world.biomes.Biome;

public enum CoreBiome implements Biome {
    MOUNTAINS("Mountains",  0.95f, 0.4f, 0.25f),
    SNOW("Snow", 1.0f, 0.75f, 0.15f),
    DESERT("Desert", 0.0f, 0.15f, 0.75f),
    FOREST("Forest", 0.9f, 0.5f, 0.5f),
    OCEAN("Ocean", 0.2f, 1.0f, 0.2f),
    BEACH("Beach", 0.1f, 0.5f, 0.2f),
    PLAINS("Plains", 0.0f, 0.5f, 0.6f);

    private final String id;
    private final String name;
    private final float fog;
    private final float humidity;
    private final float temperature;

    private CoreBiome(String name, float fog, float humidity, float temperature) {
        this.id = "Core:" + name().toLowerCase();
        this.name = name;
        this.fog = fog;
        this.humidity = humidity;
        this.temperature = temperature;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getFog() {
        return fog;
    }

    @Override
    public float getHumidity() {
        return humidity;
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

}
