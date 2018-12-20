/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.module.sandbox.API;

/**
 * Biomes can be assigned to different blocks during worldgen as well as on runtime, to provide additional metadata
 * about player's surroundings usable to enhance player experience.
 * @see BiomeChangeEvent
 */
@API
public interface Biome {

    /**
     * @return An identifier that includes both the Module the biome originates from
     * and a unique biome id (unique to that module).
     */
    String getId();

    /**
     * Returns human readable name of the biome.
     */
    String getName();

    @Deprecated
    default float getFog() {
        return 0.5f;
    }

    @Deprecated
    default float getHumidity() {
        return 0.5f;
    }

    @Deprecated
    default float getTemperature() {
        return 0.5f;
    }

}
