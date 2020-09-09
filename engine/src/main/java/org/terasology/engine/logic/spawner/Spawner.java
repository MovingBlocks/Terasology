// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.spawner;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.generation.World;
import org.terasology.math.geom.Vector3f;

/**
 * Defines spawning points.
 */
@FunctionalInterface
public interface Spawner {

    Vector3f getSpawnPosition(World world, EntityRef entity);
}
