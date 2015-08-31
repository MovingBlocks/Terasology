/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.generator;


import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.spawner.FixedSpawner;
import org.terasology.math.geom.Vector3f;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.World;

public interface WorldGenerator {
    SimpleUri getUri();

    String getWorldSeed();

    void setWorldSeed(String seed);

    void createChunk(CoreChunk chunk, EntityBuffer buffer);

    void initialize();

    WorldConfigurator getConfigurator();

    World getWorld();

    /**
     * Determines a spawn position suitable for this world, such as that used to spawn the initial player.
     * The default implementation simply picks a position in the very center of the world.
     * @param entity the entity related to spawning, if needed (or not). Can be ignored.
     * @return the chosen position
     */
    default Vector3f getSpawnPosition(EntityRef entity) {
        return new FixedSpawner(0, 0).getSpawnPosition(getWorld(), entity);
    }
}
