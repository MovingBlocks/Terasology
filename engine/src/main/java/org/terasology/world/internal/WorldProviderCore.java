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
package org.terasology.world.internal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;

/**
 * Provides the basic interface for all world providers.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface WorldProviderCore {
    /**
     * Returns the world entity.
     *
     * @return the entity of the world
     */
    public EntityRef getWorldEntity();

    /**
     * Returns the title of this world.
     *
     * @return the title of this world
     */
    String getTitle();

    /**
     * Returns the seed of this world.
     *
     * @return The seed value
     */
    String getSeed();

    /**
     * @return General world info
     */
    WorldInfo getWorldInfo();

    /**
     * Process any propagation, such as light
     */
    void processPropagation();

    /**
     * @param listener
     */
    void registerListener(WorldChangeListener listener);

    void unregisterListener(WorldChangeListener listener);

    /**
     * @param chunkPos
     * @return A world view centered on the desired chunk, with the surrounding chunks present.
     */
    ChunkViewCore getLocalView(Vector3i chunkPos);

    /**
     * @param chunk
     * @return A world view of the chunks around the desired chunk, uncentered.
     */
    ChunkViewCore getWorldViewAround(Vector3i chunk);


    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param x
     * @param y
     * @param z
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(int x, int y, int z);

    /**
     * Places a block of a specific type at a given position
     *
     * @param pos  The world position to change
     * @param type The type of the block to set
     * @return The previous block type. Null if the change failed (because the necessary chunk was not loaded)
     */
    Block setBlock(Vector3i pos, Block type);

    /**
     * @param x
     * @param y
     * @param z
     * @param newData
     * @param oldData
     * @return Whether the liquid change was made successfully. Will fail if the current data doesn't match the oldData, or if the underlying chunk is not available
     */
    boolean setLiquid(int x, int y, int z, LiquidData newData, LiquidData oldData);

    /**
     * Returns the liquid state at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The liquid data of the block
     */
    LiquidData getLiquid(int x, int y, int z);

    /**
     * Returns the block at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The type of the block
     */
    Block getBlock(int x, int y, int z);

    /**
     * Returns the light value at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The light value
     */
    byte getLight(int x, int y, int z);

    /**
     * Returns the sunlight value at the given position
     *
     * @param x
     * @param y
     * @param z
     * @return The sunlight value
     */
    byte getSunlight(int x, int y, int z);

    byte getTotalLight(int x, int y, int z);

    /**
     * Disposes this world provider.
     */
    void dispose();

    WorldTime getTime();

    float getFog(float x, float y, float z);

    float getTemperature(float x, float y, float z);

    float getHumidity(float x, float y, float z);
}
