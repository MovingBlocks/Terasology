// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator;


import org.joml.Vector3fc;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.spawner.FixedSpawner;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.EntityBuffer;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.zones.Zone;

import java.util.List;

/**
 * World generator is an interface responsible for generating worlds from their seed
 */
public interface WorldGenerator {
    /**
     * @return Uri of the current world generator instance
     */
    SimpleUri getUri();

    /**
     * @return Seed used for creating this world generator
     */
    String getWorldSeed();

    /**
     * Sets the seed to use for creating of the world made by this world generator.
     * <p>
     * NOTE: this is a String value. The long value used in {@link BaseFacetedWorldGenerator},
     * which is the most commonly used implementation of this interface, is calculated as hash of this String value.
     *
     * @param seed Value of the seed
     */
    void setWorldSeed(String seed);

    /**
     * Generates all contents of given chunk
     * @param chunk Chunk to generate
     * @param buffer Buffer to queue entities to spawn to
     */
    void createChunk(CoreChunk chunk, EntityBuffer buffer);

    /**
     * Performs any additional steps required for setting itself up before generating world
     */
    void initialize();

    /**
     * @return Associated world configurator
     */
    WorldConfigurator getConfigurator();

    /**
     * @return Generated world
     */
    World getWorld();

    default List<Zone> getZones() {
        return null;
    }

    default Zone getNamedZone(String name) {
        return null;
    }

    /**
     * Determines a spawn position suitable for this world, such as that used to spawn the initial player.
     * The default implementation simply picks a position in the very center of the world.
     *
     * @param entity the entity related to spawning, if needed (or not). Can be ignored.
     * @return the chosen position
     */
    default Vector3fc getSpawnPosition(EntityRef entity) {
        return new FixedSpawner(0, 0).getSpawnPosition(getWorld(), entity);
    }
}
