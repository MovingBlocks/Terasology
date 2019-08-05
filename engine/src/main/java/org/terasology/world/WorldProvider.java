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

import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.internal.WorldProviderCore;

/**
 * Provides the basic interface for all world providers.
 *
 */
public interface WorldProvider extends WorldProviderCore {

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param pos
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(Vector3i pos);

    boolean isBlockRelevant(Vector3f pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(Vector3f pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(Vector3i pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3f pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3f pos);

    byte getTotalLight(Vector3f pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3i pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3i pos);

    byte getTotalLight(Vector3i pos);
    
    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param index The index of the extra data field
     * @param pos
     * @return The (index)th extra-data value at the given position
     */
    int getExtraData(int index, Vector3i pos);
    
    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     *
     * @param index The index of the extra data field
     * @param x
     * @param y
     * @param z
     * @param value
     * @return The replaced value
     */
    int setExtraData(int index, int x, int y, int z, int value);
    
    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param fieldName The name of the extra-data field
     * @param x
     * @param y
     * @param z
     * @return The named extra-data value at the given position
     */
    int getExtraData(String fieldName, int x, int y, int z);
    
    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param fieldName The name of the extra-data field
     * @param pos
     * @return The named extra-data value at the given position
     */
    int getExtraData(String fieldName, Vector3i pos);
    
    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     *
     * @param fieldName The name of the extra-data field
     * @param x
     * @param y
     * @param z
     * @param value
     * @return The replaced value
     */
    int setExtraData(String fieldName, int x, int y, int z, int value);
    
    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     *
     * @param fieldName The name of the extra-data field
     * @param pos
     * @param value
     * @return The replaced value
     */
    int setExtraData(String fieldName, Vector3i pos, int value);
}
