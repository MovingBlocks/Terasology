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

import com.google.common.collect.Maps;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;

import java.util.Collection;
import java.util.Map;

/**
 * Provides the basic interface for all world providers.
 */
public interface WorldProviderCore {

    /**
     * Returns the world entity.
     *
     * @return the entity of the world
     */
    EntityRef getWorldEntity();

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

    boolean isRegionRelevant(Region3i region);

    /**
     * Places a block of a specific type at a given position
     *
     * @param pos  The world position to change
     * @param type The type of the block to set
     * @return The previous block type. Null if the change failed (because the necessary chunk was not loaded)
     */
    Block setBlock(Vector3i pos, Block type);

    /**
     * Places all given blocks of specific types at their corresponding positions
     * </p>
     * Chunks are
     *
     * @param blocks A mapping from world position to change to the type of block to set
     * @return A mapping from world position to previous block type.
     * The value of a map entry is Null if the change failed (because the necessary chunk was not loaded)
     */
    default Map<Vector3i, Block> setBlocks(Map<Vector3i, Block> blocks) {
        Map<Vector3i, Block> resultMap = Maps.newHashMap();
        for (Map.Entry<Vector3i, Block> entry: blocks.entrySet()) {
            Block oldBlock = setBlock(entry.getKey(), entry.getValue());
            resultMap.put(entry.getKey(), oldBlock);
        }
        return resultMap;
    }



    /**
     * Changes the biome at the given position.
     *
     * @param pos   The world position to change
     * @param biome The biome to set
     * @return The previous biome type at the position. Null if the change failed (because the necessary chunk was not loaded)
     */
    Biome setBiome(Vector3i pos, Biome biome);

    /**
     * Returns the biome at a specific world position.
     *
     * @param pos The position
     * @return The biome at the given position.
     */
    Biome getBiome(Vector3i pos);

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
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     *
     * @param index The index of the extra data field
     * @param pos
     * @param value
     * @return The replaced value
     */
    int setExtraData(int index, Vector3i pos, int value);

    /**
     * Disposes this world provider.
     */
    void dispose();

    WorldTime getTime();

    /**
     * @return an unmodifiable view on the generated relevant regions
     */
    Collection<Region3i> getRelevantRegions();
}
