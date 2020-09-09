// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks;

import org.terasology.math.geom.Vector3i;

/**
 *
 */
public interface ChunkRegionListener {

    /**
     * Invoked when a chunk has entered relevance for this chunk region (may be just loaded, or region may have moved to
     * include it)
     *
     * @param pos
     * @param chunk
     */
    void onChunkRelevant(Vector3i pos, Chunk chunk);

    /**
     * Invoked when a chunk ceases to be relevant for this chunk region (
     *
     * @param pos
     */
    void onChunkIrrelevant(Vector3i pos);
}
