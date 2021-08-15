// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.internal.ChunkViewCore;

import java.util.Collection;

/**
 * Provides Chunks and view for it.
 */
public interface ChunkProvider {

    /**
     * A view that gives access to a subset of the chunks
     * @param region the region to include, in chunk coordinates
     * @param offset the offset for the chunk view, in chunk coordinates
     * @return
     */
    ChunkViewCore getSubview(BlockRegionc region, Vector3ic offset);

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
    boolean reloadChunk(Vector3ic pos);

    /**
     * Purges all chunks that are currently loaded and force their re-generation.
     */
    void purgeWorld();

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
    Chunk getChunk(Vector3ic chunkPos);

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
