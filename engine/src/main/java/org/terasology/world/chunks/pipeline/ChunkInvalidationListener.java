// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * Notified on invalidation {@link Chunk} at position.
 */
@FunctionalInterface
public interface ChunkInvalidationListener {
    /**
     * Fire on invalidation chunk
     * @param pos position of chunk
     */
    void onInvalidation(Vector3i pos);
}
