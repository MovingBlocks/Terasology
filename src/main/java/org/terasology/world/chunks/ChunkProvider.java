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

package org.terasology.world.chunks;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public interface ChunkProvider {

    /**
     * Sets the world entity, for the purpose of receiving chunk events.
     *
     * @param entity
     */
    public void setWorldEntity(EntityRef entity);

    /**
     * Requests that a region around the given entity be maintained in near cache
     *
     * @param entity
     * @param distance The region (in chunks) around the entity that should be near cached
     */
    public void addRelevanceEntity(EntityRef entity, int distance);

    /**
     * Requests that a region around the given entity be maintained in near cache
     *
     * @param entity
     * @param distance The region (in chunks) around the entity that should be near cached
     * @param listener A listener to chunk region events
     */
    public void addRelevanceEntity(EntityRef entity, int distance, ChunkRegionListener listener);

    /**
     * Retrieves the ChunkRelevanceRegion object for the given entity
     *
     * @param entity
     * @return The chunk relevance region, or null
     */
    public void updateRelevanceEntity(EntityRef entity, int distance);

    /**
     * Removes an entity from producing a caching region
     *
     * @param entity
     */
    public void removeRelevanceEntity(EntityRef entity);

    /**
     * Updates the near cache based on the movement of the caching entities
     */
    public void update();

    /**
     * @param pos
     * @return Whether this chunk is readily available - does not need to be built or remotely retrieved
     */
    public boolean isChunkAvailable(Vector3i pos);

    /**
     * Returns the chunk at the given position if possible.
     *
     * @param x The chunk position on the x-axis
     * @param y The chunk position on the y-axis
     * @param z The chunk position on the z-axis
     * @return The chunk, or null if it cannot be obtained
     */
    public Chunk getChunk(int x, int y, int z);

    public Chunk getChunk(Vector3i chunkPos);

    /**
     * Disposes all chunks managed by this chunk provider.
     */
    public void dispose();

    /**
     * Returns the amount of chunks managed by this chunk provider.
     *
     * @return The amount of managed chunks
     */
    public float size();

    boolean isChunkReady(Vector3i pos);
}
