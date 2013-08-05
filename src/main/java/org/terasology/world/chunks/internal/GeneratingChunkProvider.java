/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world.chunks.internal;

import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.Chunk;

/**
 * Internal interface used within the chunk generation system, allows a chunk provider to manage "generation" (including
 * reloading) of a chunk. These methods may be called off of the main thread.
 *
 * @author Immortius
 */
public interface GeneratingChunkProvider {

    /**
     * @param pos
     * @return Whether this chunk is available and ready for use
     */
    boolean isChunkReady(Vector3i pos);

    /**
     * Obtains a chunk for pipeline processing. This should happen regardless of the state of the chunk.
     *
     * @param pos
     * @return The requested chunk, or null if it isn't currently loaded.
     */
    Chunk getChunkForProcessing(Vector3i pos);

    /**
     * Obtains a local chunk view of the chunk at the given position and the immediately surrounding chunks.
     * Block positions are offset so that the origin is at minimum coords of the target chunk.
     *
     * @param chunkPos
     * @return A local chunk view, or null if some of the chunks are unavailable.
     */
    ChunkView getViewAround(Vector3i chunkPos);

    /**
     * Causes the creation or loading of a chunk.
     *
     * @param position
     */
    void createOrLoadChunk(Vector3i position);

    /**
     * Notifies the chunk provider that a chunk is ready.
     *
     * @param position
     */
    void onChunkIsReady(Vector3i position);
}
