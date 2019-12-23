/*
 * Copyright 2013 MovingBlocks
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

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;

/**
 * Internal interface used within the chunk generation system, allows a chunk provider to manage "generation" (including
 * reloading) of a chunk. These methods may be called off of the main thread.
 *
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
