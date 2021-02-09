// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.joml.Vector3ic;
import org.terasology.world.chunks.Chunk;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * Function-style chunk task.
 */
public interface ChunkTask extends Function<Chunk, Chunk> {
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
    default Set<Vector3ic> getRequirements() {
        return Collections.singleton(getPosition());
    }
}
