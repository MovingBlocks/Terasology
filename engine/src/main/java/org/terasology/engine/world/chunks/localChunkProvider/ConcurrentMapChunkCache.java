// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import com.google.common.collect.Maps;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

class ConcurrentMapChunkCache implements ChunkCache {

    private final Map<Vector3i, Chunk> cache = Maps.newConcurrentMap();

    @Override
    public Chunk get(final Vector3i chunkPosition) {
        return cache.get(chunkPosition);
    }

    @Override
    public void put(final Vector3i chunkPosition, final Chunk chunk) {
        cache.put(chunkPosition, chunk);
    }

    @Override
    public Iterator<Vector3i> iterateChunkPositions() {
        return cache.keySet().iterator();
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return cache.values();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean containsChunkAt(final Vector3i chunkPosition) {
        return cache.containsKey(chunkPosition);
    }

    @Override
    public void removeChunkAt(final Vector3i chunkPosition) {
        cache.remove(chunkPosition);
    }
}
