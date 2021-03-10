// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.spawner;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.generation.World;

/**
 * Defines spawning points.
 */
@FunctionalInterface
public interface Spawner {

    Vector3f getSpawnPosition(World world, EntityRef entity);
}
