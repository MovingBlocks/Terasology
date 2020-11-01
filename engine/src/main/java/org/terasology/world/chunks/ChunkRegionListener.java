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

package org.terasology.world.chunks;

import org.joml.Vector3ic;
import org.terasology.math.geom.Vector3i;

/**
 */
public interface ChunkRegionListener {

    /**
     * Invoked when a chunk has entered relevance for this chunk region (may be just loaded, or region may have moved
     * to include it)
     *
     * @param pos
     * @param chunk
     */
    void onChunkRelevant(Vector3ic pos, Chunk chunk);

    /**
     * Invoked when a chunk ceases to be relevant for this chunk region (
     *
     * @param pos
     */
    void onChunkIrrelevant(Vector3ic pos);
}
