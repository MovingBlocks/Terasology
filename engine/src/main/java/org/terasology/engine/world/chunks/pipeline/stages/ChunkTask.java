// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline.stages;

import com.google.common.collect.Lists;
import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Function-style chunk task.
 */
public interface ChunkTask extends Function<Collection<Chunk>, Chunk> {
    /**
     * Task name. used for ThreadMonitor.
     *
     * @return task name.
     */
    String getName();

    /**
     * Chunk task position. used for ChunkTask Sorting.
     *
     * @return
     */
    Vector3ic getPosition();

    /**
     * Requirement another chunk at positions for this chunk task.
     *
     * @return required positions for processing.
     */
    default List<Vector3ic> getRequirements() {
        return Lists.newArrayList(getPosition());
    }
}
