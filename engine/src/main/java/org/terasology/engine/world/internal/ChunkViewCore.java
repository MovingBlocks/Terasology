// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.internal;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.ChunkView;

public interface ChunkViewCore extends ChunkView {

    /**
     * Sets the light level at the given position. the value is usually bounded by {@link Chunks#MAX_LIGHT}
     *
     * @param pos   The position relative to the corner of the chunk
     * @param light set the light value of a block.
     */
    void setLight(Vector3ic pos, byte light);

    /**
     * Sets the light level at the given coordinates. the value is usually bounded by {@link Chunks#MAX_LIGHT}
     *
     * @param blockX X offset from the corner of the chunk
     * @param blockY Y offset from the corner of the chunk
     * @param blockZ Z offset from the corner of the chunk
     * @param light  set the light value of a block.
     */
    void setLight(int blockX, int blockY, int blockZ, byte light);


    /**
     * Sets the sunlight level at the given position. the value is usually bounded by {@link Chunks#MAX_SUNLIGHT}
     *
     * @param pos   The position relative to the corner of the chunk
     * @param light set the sunlight light value of a block.
     */
    void setSunlight(Vector3ic pos, byte light);


    /**
     * Sets the sunlight level at the given coordinates. the value is usually bounded by {@link Chunks#MAX_SUNLIGHT}
     *
     * @param blockX X offset from the corner of the chunk
     * @param blockY Y offset from the corner of the chunk
     * @param blockZ Z offset from the corner of the chunk
     * @param light  set the sunlight light value of a block.
     */
    void setSunlight(int blockX, int blockY, int blockZ, byte light);

}
