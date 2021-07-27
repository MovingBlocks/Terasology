// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import org.joml.Vector3i;
import org.terasology.engine.world.chunks.Chunk;

/**
 * A chunk store is used to save a chunk and its entity contents.
 *
 */
public interface ChunkStore {

    /**
     * @return The position of the chunk in its world
     */
    Vector3i getChunkPosition();

    /**
     * @return The chunk itself
     */
    Chunk getChunk();

    /**
     * Restores all the entities stored with this chunk
     */
    void restoreEntities();

}
