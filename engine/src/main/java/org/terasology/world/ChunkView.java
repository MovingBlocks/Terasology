/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegionc;

/**
 * A chunk view is a way of accessing multiple chunks for modification in a performant manner.
 * Chunk views also support relative subviewing - looking at an area of the world with a uniform offset to block positions
 * <br><br>
 * ChunkViews must be locked before write operations can be enacted - any write operations requested outside of a lock
 * are ignored.
 *
 */
public interface ChunkView {
    /**
     * @param x
     * @param y
     * @param z
     * @return The block at the given coordinates. If this is outside of the view then the air block is returned
     */
    Block getBlock(float x, float y, float z);

    /**
     * @param pos
     * @return The block at the given position. If this is outside of the view then the air block is returned
     */
    Block getBlock(Vector3ic pos);

    /**
     * @param x
     * @param y
     * @param z
     * @return The block at the given coordinates. If this is outside of the view then the air block is returned
     */
    Block getBlock(int x, int y, int z);

    /**
     * @param x
     * @param y
     * @param z
     * @return The value of sunlight at the given coordinates, or 0 if outside the view.
     */
    byte getSunlight(float x, float y, float z);

    /**
     * @param pos
     * @return The value of sunlight at the given position, or 0 if outside the view.
     */
    byte getSunlight(Vector3i pos);

    /**
     * @param x
     * @param y
     * @param z
     * @return The value of sunlight at the given coordinates, or 0 if outside the view.
     */
    byte getSunlight(int x, int y, int z);

    /**
     * @param x
     * @param y
     * @param z
     * @return The value of light at the given coordinates, or 0 if outside the view.
     */
    byte getLight(float x, float y, float z);

    /**
     * @param pos
     * @return The value of light at the given position, or 0 if outside the view.
     */
    byte getLight(Vector3i pos);

    /**
     * @param x
     * @param y
     * @param z
     * @return The value of light at the given coordinates, or 0 if outside the view.
     */
    byte getLight(int x, int y, int z);

    /**
     * Sets the block at the given position, if it is within the view.
     *
     * @param pos
     * @param type
     */
    void setBlock(Vector3ic pos, Block type);

    /**
     * Sets the block at the given coordinates, if it is within the view.
     *
     * @param x
     * @param y
     * @param z
     * @param type
     */
    void setBlock(int x, int y, int z, Block type);

    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param index The index of the extra data field
     * @param x
     * @param y
     * @param z
     * @return The (index)th extra-data value at the given position
     */
    int getExtraData(int index, int x, int y, int z);

    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param index The index of the extra data field
     * @param pos
     * @return The (index)th extra-data value at the given position
     */
    int getExtraData(int index, Vector3ic pos);

    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     *
     * @param index The index of the extra data field
     * @param x
     * @param y
     * @param z
     * @param value
     */
    void setExtraData(int index, int x, int y, int z, int value);

    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     *
     * @param index The index of the extra data field
     * @param pos
     * @param value
     */
    void setExtraData(int index, Vector3ic pos, int value);

    /**
     * Converts a coordinate from view-space to world space.
     *
     * @param localPos
     * @return The equivalent world-space coordinate for the given view coord.
     */
    Vector3i toWorldPos(Vector3ic localPos);

    /**
     * @return The region of the world which this view is over
     */
    BlockRegionc getWorldRegion();

    /**
     * @return A Region3i denoting the chunks covered by this view
     */
    BlockRegionc getChunkRegion();

    /**
     * Sets the chunks containing or adjacent to blockPos, which are contained in the chunk view, to dirty. This causes
     * their mesh to be regenerated.
     *
     * @param blockPos
     */
    void setDirtyAround(Vector3ic blockPos);

    /**
     * Sets ths chunks contained or adjacent to blockRegion, which are contained in the chunk view, to dirty. This causes
     * their mesh to be regenerated.
     *
     * @param blockRegion
     */
    void setDirtyAround(BlockRegionc blockRegion);


    /**
     * @return Whether the chunk view is still valid - will be false if a chunk has been unloaded since the chunk
     * view was created. Should be checked within a lock.
     */
    boolean isValidView();
}
