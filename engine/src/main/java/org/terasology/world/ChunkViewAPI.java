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

import org.terasology.engine.API;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

@API
public interface ChunkViewAPI {
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
}
