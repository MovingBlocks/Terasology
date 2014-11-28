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
package org.terasology.world.biomes;

/**
 * Represents a Biome that can be used in cases where the real biome is not yet known, i.e.
 * when the chunk containing the requested block is not yet loaded.
 */
final class UnknownBiome implements Biome {

    public static final UnknownBiome INSTANCE = new UnknownBiome();

    private UnknownBiome() {
    }

    @Override
    public String getId() {
        return "engine:unknown";
    }

    @Override
    public float getHumidity() {
        return 0.5f;
    }

    @Override
    public float getTemperature() {
        return 0.5f;
    }

    @Override
    public String getName() {
        return "Unknown";
    }

    @Override
    public float getFog() {
        return 0;
    }

}
