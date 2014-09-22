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

import org.terasology.engine.SimpleUri;
import org.terasology.world.biomes.Biome;

public enum CoreBiome implements Biome {
    MOUNTAINS("Mountains", true, 0.95f, 0.4f, 0.25f),
    SNOW("Snow", false, 1.0f, 0.75f, 0.15f),
    DESERT("Desert", true, 0.0f, 0.15f, 0.75f),
    FOREST("Forest", true, 0.9f, 0.5f, 0.5f),
    PLAINS("Plains", true, 0.0f, 0.5f, 0.6f);

    private final String id;
    private final String name;
    private final boolean vegetationFriendly;
    private final float fog;
    private final float humidity;
    private final float temperature;

    private CoreBiome(String name, boolean vegetationFriendly, float fog, float humidity, float temperature) {
        this.id = "Core:" + name().toLowerCase();
        this.name = name;
        this.vegetationFriendly = vegetationFriendly;
        this.fog = fog;
        this.humidity = humidity;
        this.temperature = temperature;
    }

    public boolean isVegetationFriendly() {
        return vegetationFriendly;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return this.name;
    }

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
