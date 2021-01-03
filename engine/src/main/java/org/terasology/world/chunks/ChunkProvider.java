// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks;

import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.internal.ChunkViewCore;

import java.util.Collection;

/**
 * Provides Chunks and view for it.
 */
public interface ChunkProvider {

    /**
     * A local view provides a
     *
     * @param centerChunkPos
     * @return A chunk view centered on the given chunk, with all of the surrounding chunks included.
     */
    ChunkViewCore getLocalView(Vector3i centerChunkPos);

    /**
     * @param blockPos
     * @param extent
     * @return A chunk view of the chunks around the given blockPos.
     */
    ChunkViewCore getSubviewAroundBlock(Vector3i blockPos, int extent);

    /**
     * @param chunkPos
     * @return A chunk view including the chunks around the given chunk
     */
    ChunkViewCore getSubviewAroundChunk(Vector3i chunkPos);

    /**
     * Sets the world entity, for the purpose of receiving chunk events.
     *
     * @param entity
     */
    void setWorldEntity(EntityRef entity);

    /**
     * Updates the near cache based on the movement of the caching entities
     */
    void update();

    /**
     * @param pos the chunk coordinates
     * @return whether this chunk was purged successfully or not
     */
    boolean reloadChunk(Vector3i pos);

    /**
     * Purges all chunks that are currently loaded and force their re-generation.
     */
    void purgeWorld();

    /**
     * @param pos
     * @return Whether this chunk is available and ready for use
     */
    boolean isChunkReady(Vector3i pos);

    /**
     * @param pos
     * @return Whether this chunk is available and ready for use
     */
    boolean isChunkReady(Vector3ic pos);

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

    /**
     * Shutdowns all threads of the chunk provider. This is used to create a save game from a consisent state.
     */
    void shutdown();

    Collection<Chunk> getAllChunks();

    /**
     * Restarts all thread activity of the chunk provider.
     */
    void restart();
}
