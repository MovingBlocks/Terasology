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

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

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
    Block getBlock(Vector3i pos);

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
     * @return The biome at the given coordinates. If this is outside of the view then the default biome is returned
     */
    Biome getBiome(float x, float y, float z);

    /**
     * @param pos
     * @return The biome at the given coordinates. If this is outside of the view then the default biome is returned
     */
    Biome getBiome(Vector3i pos);

    /**
     * @param x
     * @param y
     * @param z
     * @return The biome at the given coordinates. If this is outside of the view then the default biome is returned
     */
    Biome getBiome(int x, int y, int z);

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
    void setBlock(Vector3i pos, Block type);

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
     * Sets the biome at the given position, if it is within the view.
     *
     * @param pos
     * @param biome
     */
    void setBiome(Vector3i pos, Biome biome);

    /**
     * Sets the biome at the given coordinates, if it is within the view.
     *
     * @param x
     * @param y
     * @param z
     * @param biome
     */
    void setBiome(int x, int y, int z, Biome biome);

    /**
     * @param pos
     * @return The state of liquid at the given position. This will be no liquid outside the view.
     */
    LiquidData getLiquid(Vector3i pos);

    /**
     * @param x
     * @param y
     * @param z
     * @return The state of liquid at the given position. This will be no liquid outside the view.
     */
    LiquidData getLiquid(int x, int y, int z);

    /**
     * Sets the liquid state at the given position, if it is within the view
     *
     * @param pos
     * @param newState
     */
    void setLiquid(Vector3i pos, LiquidData newState);

    /**
     * Sets the liquid state at the given position, if it is within the view
     *
     * @param x
     * @param y
     * @param z
     * @param newState
     */
    void setLiquid(int x, int y, int z, LiquidData newState);

    /**
     * Converts a coordinate from view-space to world space.
     *
     * @param localPos
     * @return The equivalent world-space coordinate for the given view coord.
     */
    Vector3i toWorldPos(Vector3i localPos);

    /**
     * @return The region of the world which this view is over
     */
    Region3i getWorldRegion();

    /**
     * @return A Region3i denoting the chunks covered by this view
     */
    Region3i getChunkRegion();

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
    void writeLock();

    /**
     * Unlocks the chunk view, disabling write operations
     */
    void writeUnlock();

    /**
     * Locks the chunk view, enabling write operations
     */
    void readLock();

    /**
     * Unlocks the chunk view, disabling write operations
     */
    void readUnlock();

    /**
     * @return Whether the chunk view is locked and hence whether write operations are allowed.
     */
    boolean isLocked();

    /**
     * @return Whether the chunk view is still valid - will be false if a chunk has been unloaded since the chunk
     * view was created. Should be checked within a lock.
     */
    boolean isValidView();
}
