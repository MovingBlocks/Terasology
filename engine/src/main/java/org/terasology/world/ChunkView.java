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

package org.terasology.world;

import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

/**
 * A chunk view is a way of accessing multiple chunks for modification in a performant manner.
 * Chunk views also support relative subviewing - looking at an area of the world with a uniform offset to block positions
 * <p/>
 * ChunkViews must be locked because write operations can be enacted - any write operations requested outside of a lock
 * are ignored.
 *
 * @author Immortius
 */
public interface ChunkView extends ChunkViewAPI {

    /***
     * @return The region of the world which this view is over
     */
    Region3i getWorldRegion();

    /**
     * @return A Region3i denoting the chunks covered by this view
     */
    Region3i getChunkRegion();

    /**
     * Sets the light level at the given position
     *
     * @param pos
     * @param light
     */
    void setLight(Vector3i pos, byte light);

    /**
     * Sets the light level at the given coordinates
     *
     * @param blockX
     * @param blockY
     * @param blockZ
     * @param light
     */
    void setLight(int blockX, int blockY, int blockZ, byte light);

    /**
     * Sets the sunlight level at the given position
     *
     * @param pos
     * @param light
     */
    void setSunlight(Vector3i pos, byte light);

    /**
     * Sets the sunlight level at the given coordinates
     *
     * @param blockX
     * @param blockY
     * @param blockZ
     * @param light
     */
    void setSunlight(int blockX, int blockY, int blockZ, byte light);

    /**
     * Sets the chunks containing or adjacent to blockPos, which are contained in the chunk view, to dirty. This causes
     * their mesh to be regenerated.
     *
     * @param blockPos
     */
    void setDirtyAround(Vector3i blockPos);

    /**
     * Sets ths chunks contained or adjacent to blockRegion, which are contained in the chunk view, to dirty. This causes
     * their mesh to be regenerated.
     *
     * @param blockRegion
     */
    void setDirtyAround(Region3i blockRegion);

    /**
     * Locks the chunk view, enabling write operations
     */
    void lock();

    /**
     * Unlocks the chunk view, disabling write operations
     */
    void unlock();

    /**
     * @return Whether the chunk view is locked and hence whether write operations are allowed.
     */
    boolean isLocked();

    /**
     * @return Whether the chunk view is still valid - will be false if a chunk has been unloaded since the chunk
     *         view was created. Should be checked within a lock.
     */
    boolean isValidView();

}
