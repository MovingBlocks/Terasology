// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * Notified on removing {@link Chunk} from {@link ChunkProcessingPipeline} at position.
 */
@FunctionalInterface
public interface ChunkRemoveFromPipelineListener {
    /**
     * Fire on invalidation chunk
     * @param pos position of chunk
     */
    void onRemove(Vector3i pos);
}
