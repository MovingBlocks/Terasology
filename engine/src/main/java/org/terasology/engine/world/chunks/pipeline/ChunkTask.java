// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.engine.utilities.concurrency.Task;
import org.terasology.engine.world.chunks.Chunk;

/**
 * @deprecated Use {@link org.terasology.engine.world.chunks.pipeline.stages.ChunkTask} instead
 */
@Deprecated
public interface ChunkTask extends Task {

    Chunk getChunk();

    default Vector3i getPosition() {
        return new Vector3i();
    }
}
