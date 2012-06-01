/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.world;

import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

import javax.vecmath.Vector3f;

/**
 * Provides the basic interface for all world providers.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface WorldProvider extends WorldProviderCore {

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param pos
     * @return Whether the given block is active
     */
    public boolean isBlockActive(Vector3i pos);

    public boolean isBlockActive(Vector3f pos);
    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     *
     * @param pos  Block position
     * @param type The type of the block to set
     * @return True if a block was set/replaced. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    public boolean setBlock(Vector3i pos, Block type, Block oldType);

    /**
     *
     * @param pos
     * @param state The new value of state
     * @param oldState The expected previous value of state
     * @return Whether the state change was made successfully. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    public boolean setState(Vector3i pos, byte state, byte oldState);

    /**
     * Returns the state at the given position.
     *
     * @param blockPos
     * @return The state of the block
     */
    public byte getState(Vector3i blockPos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public Block getBlock(Vector3f pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public Block getBlock(Vector3i pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public byte getLight(Vector3f pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public byte getSunlight(Vector3f pos);

    public byte getTotalLight(Vector3f pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public byte getLight(Vector3i pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public byte getSunlight(Vector3i pos);

    public byte getTotalLight(Vector3i pos);

}
