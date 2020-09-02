// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

/**
 * Notified on significant {@link ChunkTask} events.
 */
@FunctionalInterface
public interface ChunkTaskListener {

    /**
     * Notified on {@link ChunkTask} done.
     * @param chunkTask ChunkTask which done processing.
     */
    void onDone(ChunkTask chunkTask);
}
