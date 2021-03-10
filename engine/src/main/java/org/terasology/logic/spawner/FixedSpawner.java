// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.spawner;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.generation.World;

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
        return findSpawnPosition(world, desiredPos, searchRadius);
    }

}
