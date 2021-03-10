// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation;

import org.terasology.engine.entitySystem.entity.EntityStore;

/**
 * A buffer for {@link EntityStore} instances.
 */
@FunctionalInterface
public interface EntityBuffer {

    void enqueue(EntityStore entity);
}
