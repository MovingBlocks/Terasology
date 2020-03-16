/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks.localChunkProvider;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;

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
