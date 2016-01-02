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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.ModuleEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BiomeManager implements BiomeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(BiomeManager.class);

    // Bi Di map between biome short id and biome
    private final BiMap<Short, Biome> biomeShortIdMap = HashBiMap.create();

    // Map from biome id to biome
    private final BiMap<String, Biome> biomeIdMap = HashBiMap.create();

    /**
     * Creates a new Biome Manager that auto discovers all biomes in the given environment.
     */
    public BiomeManager(ModuleEnvironment environment) {
        this(environment, Collections.<String, Short>emptyMap());
    }

    /**
     * Create a BiomeManager from known state such as a world save, that already contains
     * a mapping between Biome URIs and their short ids.
     *
     * @param knownBiomeIdMap A mapping between Biome URIs (combination of module id + biome id) and
     *                        their short ids that are applicable to this world save.
     */
    public BiomeManager(ModuleEnvironment moduleEnvironment, Map<String, Short> knownBiomeIdMap) {

        for (Class<?> biomeRegistrator : moduleEnvironment.getSubtypesOf(BiomeRegistrator.class)) {

            BiomeRegistrator registrator;
            try {
                registrator = (BiomeRegistrator) biomeRegistrator.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Cannot call biome registrator {} because it cannot be instantiated.", biomeRegistrator, e);
                continue;
            }

            registrator.registerBiomes(this);

        }

        BiMap<Short, Biome> currentIdMap = HashBiMap.create(biomeShortIdMap); // Make a copy before we start modifying it
        biomeShortIdMap.clear();

        // Always register the unknown biome first, so it gets id 0, which is the default for all chunks
        registerBiome(UnknownBiome.INSTANCE);

        for (Map.Entry<String, Short> entry : knownBiomeIdMap.entrySet()) {
            if (entry.getKey().equals(getUnknownBiome().getId())) {
                continue; // The unknown biome is handled internally
            }

            Biome biome = biomeIdMap.get(entry.getKey());
            if (biome == null) {
                throw new IllegalStateException("Save game references biome " + entry.getKey() +  " which is no " +
                    "longer available.");
            }
            if (biomeShortIdMap.put(entry.getValue(), biome) != null) {
                throw new IllegalStateException("Biome short id " + entry.getValue() + " is present multiple times " +
                    "in the save game (latest is mapped to " + biome.getId() + ".");
            }
            logger.info("Restored biome {} with short id {} from save game.", entry.getKey(), entry.getValue());
            currentIdMap.values().remove(biome);
        }

        // Handle all new biomes that weren't present in the save game
        for (Biome biome : currentIdMap.values()) {
            short freeBiomeId = getFreeBiomeId();
            biomeShortIdMap.put(freeBiomeId, biome);
            logger.info("Registered new biome {} with id {} that wasn't present in the save game.", biome.getId(),
                freeBiomeId);
        }

    }

    public String getBiomeId(Biome biome) {
        return biomeIdMap.inverse().get(biome);
    }

    @Override
    public void registerBiome(Biome biome) {
        String fullId = biome.getId();

        if (biomeShortIdMap.containsValue(biome)) {
            throw new IllegalArgumentException("The biome " + fullId + " is already registered.");
        }

        if (biomeIdMap.containsKey(fullId)) {
            throw new IllegalArgumentException("A biome with id " + fullId + " is already registered!");
        }

        short biomeShortId = getFreeBiomeId();

        logger.info("Registering biome {} with short id {}.", biome, biomeShortId);

        biomeShortIdMap.put(biomeShortId, biome);
        biomeIdMap.put(fullId, biome);
    }

    @Override
    public Biome getBiomeById(String id) {
        return biomeIdMap.get(id);
    }

    @Override
    public List<Biome> getBiomes() {
        return ImmutableList.copyOf(biomeIdMap.values());
    }

    /**
     * Returns a biome with the given id and of the given biome type, only if the biome actually is of the given type.
     *
     * @param id
     * @param biomeClass
     * @param <T>
     * @return
     */
    @Override
    public <T extends Biome> T getBiomeById(String id, Class<T> biomeClass) {
        Biome biome = getBiomeById(id);
        if (biome != null) {
            if (biomeClass.isAssignableFrom(biome.getClass())) {
                return biomeClass.cast(biome);
            }
        }
        return null;
    }

    @Override
    public <T extends Biome> List<T> getBiomes(Class<T> biomeClass) {

        ImmutableList.Builder<T> builder = ImmutableList.builder();

        biomeIdMap.values().stream().filter(biome -> biomeClass.isAssignableFrom(biome.getClass())).forEach(biome ->
                builder.add(biomeClass.cast(biome)));

        return builder.build();

    }

    public Biome getBiomeByShortId(short biomeShortId) {
        Biome result = biomeShortIdMap.get(biomeShortId);
        if (result == null) {
            result = getUnknownBiome();
        }
        return result;
    }

    public short getBiomeShortId(Biome biome) {
        Short result = biomeShortIdMap.inverse().get(biome);
        if (result == null) {
            throw new IllegalArgumentException("No short id for biome " + biome + " exists.");
        }
        return result;
    }

    private short getFreeBiomeId() {
        short id = 0;
        while (id < Short.MAX_VALUE) {
            if (!biomeShortIdMap.containsKey(id)) {
                return id;
            }
            id++;
        }
        throw new IllegalStateException("The maximum number of biomes has been reached: " + biomeShortIdMap.size());
    }

    /**
     * @return A biome that can be used to avoid a null pointer in cases where the real biome is unknown, i.e.
     * when the chunk has not yet been loaded.
     * <br><br>
     * TODO: Decide how a caller can determine that he got the unknown biome and not the real one.
     */
    public static Biome getUnknownBiome() {
        return UnknownBiome.INSTANCE;
    }

}
