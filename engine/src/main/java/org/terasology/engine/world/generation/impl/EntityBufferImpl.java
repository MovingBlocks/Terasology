// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation.impl;

import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.world.generation.EntityBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
