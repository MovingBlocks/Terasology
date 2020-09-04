// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;

/**
 * Functional interface for {@link NotifyChunkTask}
 */
@FunctionalInterface
public interface ChunkPipelineTaskListener {

    void fire(Chunk chunk);
}
