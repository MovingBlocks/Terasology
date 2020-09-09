// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.internal;

import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.math.geom.Vector3i;

/**
 * Internal interface used within the chunk generation system, allows a chunk provider to manage "generation" (including
 * reloading) of a chunk. These methods may be called off of the main thread.
 */
public interface GeneratingChunkProvider extends ChunkProvider {

    /**
     * Notifies the chunk provider that a chunk is ready.
     *
     * @param chunk
     */
    void onChunkIsReady(Chunk chunk);

    Chunk getChunkUnready(Vector3i pos);
}
