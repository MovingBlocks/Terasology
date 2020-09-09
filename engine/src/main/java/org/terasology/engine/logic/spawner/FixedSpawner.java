// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.spawner;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.generation.World;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;

/**
 * Spawns all entities at a fixed location.
 */
public class FixedSpawner extends AbstractSpawner {

    private final Vector2i desiredPos;
    private final int searchRadius;

    /**
     * Search radius of 16 blocks
     *
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
