/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.spawner;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.world.generation.World;

/**
 * Spawns all entities at a fixed location.
 */
public class FixedSpawner extends AbstractSpawner {

    private final Vector2i desiredPos;
    private int searchRadius;

    /**
     * Search radius of 16 blocks
     * @param targetX
     * @param targetZ
     */
    public FixedSpawner(int targetX, int targetZ) {
        this(targetX, targetZ, 16);
    }

    public FixedSpawner(int targetX, int targetZ, int searchRadius) {
        this.searchRadius = searchRadius;
        this.desiredPos = new Vector2i(targetX, targetZ);
    }

    @Override
    public Vector3f getSpawnPosition(World world, EntityRef entity) {
        // don't care about the given entity
        Vector3f realPos = findSpawnPosition(world, desiredPos, searchRadius);
        return realPos;
    }

}
