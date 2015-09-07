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

package org.terasology.world.generation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.world.generation.EntityBuffer;

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
