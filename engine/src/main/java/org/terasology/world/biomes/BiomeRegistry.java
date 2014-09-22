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

import org.terasology.module.sandbox.API;

import java.util.List;

/**
 * Allows modules to register their biomes with the engine so they can be used in world generation.
 */
@API
public interface BiomeRegistry {

    /**
     * Register a new biome with the given id.
     *
     * @param biome The biome to register.
     */
    void registerBiome(Biome biome);

    Biome getBiomeById(String id);

    List<Biome> getBiomes();

    <T extends Biome> T getBiomeById(String id, Class<T> biomeClass);

    /**
     * Gets all biomes of a given type (or subtype).
     *
     * @param biomeClass All biomes that are assignable to this class will be returned.
     * @return An immutable list of all biomes that implement the given interface or extend the given class.
     */
    <T extends Biome> List<T> getBiomes(Class<T> biomeClass);

}
