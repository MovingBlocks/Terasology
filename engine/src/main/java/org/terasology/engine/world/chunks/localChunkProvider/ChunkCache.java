// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import org.terasology.engine.world.chunks.Chunk;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;
import java.util.Iterator;

/**
 * Chunk storage which allows to look up for chunks based on their world position.
 */
interface ChunkCache {
    Chunk get(Vector3i chunkPosition);

    void put(Vector3i chunkPosition, Chunk chunk);

    Iterator<Vector3i> iterateChunkPositions();

    Collection<Chunk> getAllChunks();

    void clear();

    boolean containsChunkAt(Vector3i chunkPosition);

    void removeChunkAt(Vector3i chunkPosition);
}
