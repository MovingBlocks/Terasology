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
public interface WorldProviderCore {

    /**
     * Returns the title of this world.
     *
     * @return the title of this world
     */
    public String getTitle();

    /**
     * Returns the seed of this world.
     *
     * @return The seed value
     */
    public String getSeed();

    /**
     *
     * @return General world info
     */
    public WorldInfo getWorldInfo();

    /**
     * @return Thw world's biome provider
     */
    public WorldBiomeProvider getBiomeProvider();

    /**
     * @param chunk
     * @return A world view centered on the desired chunk, with the surrounding chunks present.
     */
    public WorldView getWorldViewAround(Vector3i chunk);

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param x
     * @param y
     * @param z
     * @return Whether the given block is active
     */
    public boolean isBlockActive(int x, int y, int z);

    /**
     * Changes a number of blocks, if all updates are valid (oldTypes match the current block types in the given positions)
     *
     * @param updates
     * @return Whether the updates succeeded
     */
    public boolean setBlocks(BlockUpdate... updates);

    /**
     * Changes a number of blocks, if all updates are valid (oldTypes match the current block types in the given positions)
     *
     * @param updates
     * @return Whether the updates succeeded
     */
    public boolean setBlocks(Iterable<BlockUpdate> updates);

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     *
     * @param x    The X-coordinate
     * @param y    The Y-coordinate
     * @param z    The Z-coordinate
     * @param type The type of the block to set
     * @return True if a block was set/replaced. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    public boolean setBlock(int x, int y, int z, Block type, Block oldType);

    /**
     * Sets the state at the given position
     *
     * @param x
     * @param y
     * @param z
     * @param state    The new value of state
     * @param oldState The expected previous value of state
     * @return Whether the state change was made successfully. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    public boolean setState(int x, int y, int z, byte state, byte oldState);

    /**
     * Returns the state at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The state of the block
     */
    public byte getState(int x, int y, int z);

    /**
     * Returns the block at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The type of the block
     */
    public Block getBlock(int x, int y, int z);

    /**
     * Returns the light value at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The light value
     */
    public byte getLight(int x, int y, int z);

    /**
     * Returns the sunlight value at the given position
     *
     * @param x
     * @param y
     * @param z
     * @return The sunlight value
     */
    public byte getSunlight(int x, int y, int z);

    public byte getTotalLight(int x, int y, int z);

    /**
     * Returns the current time.
     *
     * @return The current time in ms from world creation
     */
    public long getTime();

    /**
     * Sets the current time of the world (in ms).
     *
     * @param time The current time
     */
    public void setTime(long time);

    public float getTimeInDays();

    public void setTimeInDays(float time);

    /**
     * Disposes this world provider.
     */
    public void dispose();

}
