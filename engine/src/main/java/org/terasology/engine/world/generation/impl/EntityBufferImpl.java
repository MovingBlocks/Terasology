// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.terasology.engine.entitySystem.entity.EntityStore;
import org.terasology.engine.world.generation.EntityBuffer;

/**
 * A list-based implementation of {@link EntityBuffer}.
 */
public class EntityBufferImpl implements EntityBuffer {

    private final List<EntityStore> queue = new ArrayList<>();

    @Override
    public void enqueue(EntityStore entity) {
        queue.add(entity);
    }

    /**
     * @return an unmodifiable list of all enqueued elements.
     */
    public List<EntityStore> getAll() {
        return Collections.unmodifiableList(queue);
    }

}
