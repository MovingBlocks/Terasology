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

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;

/**
 * @author Immortius
 */
public interface ChunkProvider {

    /**
     * A local view pvoides a
     *
     * @param centerChunkPos
     * @return A chunk view centered on the given chunk, with all of the surrounding chunks included.
     */
    ChunkView getLocalView(Vector3i centerChunkPos);

    /**
     * @param blockPos
     * @param extent
     * @return A chunk view of the chunks around the given blockPos.
     */
    ChunkView getSubviewAroundBlock(Vector3i blockPos, int extent);

    /**
     * @param chunkPos
     * @return A chunk view including the chunks around the given chunk
     */
    ChunkView getSubviewAroundChunk(Vector3i chunkPos);

    /**
     * Sets the world entity, for the purpose of receiving chunk events.
     *
     * @param entity
     */
    void setWorldEntity(EntityRef entity);

    /**
     * Requests that a region around the given entity be maintained in near cache
     *
     * @param entity
     * @param distance The region (in chunks) around the entity that should be near cached
     */
    void addRelevanceEntity(EntityRef entity, int distance);

    /**
     * Requests that a region around the given entity be maintained in near cache
     *
     * @param entity
     * @param distance The region (in chunks) around the entity that should be near cached
     * @param listener A listener to chunk region events
     */
    void addRelevanceEntity(EntityRef entity, int distance, ChunkRegionListener listener);

    /**
     * Retrieves the ChunkRelevanceRegion object for the given entity
     *
     * @param entity
     * @return The chunk relevance region, or null
     */
    void updateRelevanceEntity(EntityRef entity, int distance);

    /**
     * Removes an entity from producing a caching region
     *
     * @param entity
     */
    void removeRelevanceEntity(EntityRef entity);

    /**
     * Updates the near cache based on the movement of the caching entities
     */
    void update();

    /**
     * @param pos
     * @return Whether this chunk is available and ready for use
     */
    boolean isChunkReady(Vector3i pos);

    /**
     * Returns the chunk at the given position if possible.
     *
     * @param x The chunk position on the x-axis
     * @param y The chunk position on the y-axis
     * @param z The chunk position on the z-axis
     * @return The chunk, or null if the chunk is not ready
     */
    Chunk getChunk(int x, int y, int z);

    /**
     * Returns the chunk at the given position if possible.
     *
     * @param chunkPos The position of the chunk to obtain
     * @return The chunk, or null if the chunk is not ready
     */
    Chunk getChunk(Vector3i chunkPos);

    /**
     * Disposes the chunk provider, cleaning up all chunks and other assets it is using
     */
    void dispose();
}
