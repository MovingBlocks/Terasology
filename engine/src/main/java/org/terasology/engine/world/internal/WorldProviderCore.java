// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.internal;

import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.time.WorldTime;

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
    ChunkViewCore getLocalView(Vector3ic chunkPos);

    /**
     * @param chunk
     * @return A world view of the chunks around the desired chunk, uncentered.
     */
    ChunkViewCore getWorldViewAround(Vector3ic chunk);

    /**
     * @param region The region, in chunk coordinates, that needs to be covered.
     * @return A world view of the chunks around the desired chunk, uncentered.
     */
    ChunkViewCore getWorldViewAround(BlockRegionc region);

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param x
     * @param y
     * @param z
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(int x, int y, int z);

    boolean isRegionRelevant(BlockRegionc region);

    /**
     * Places a block of a specific type at a given position
     *
     * @param pos  The world position to change
     * @param type The type of the block to set
     * @return The previous block type. Null if the change failed (because the necessary chunk was not loaded)
     */
    Block setBlock(Vector3ic pos, Block type);

    /**
     * Places all given blocks of specific types at their corresponding positions
     * <p>
     * Chunks are
     *
     * @param blocks A mapping from world position to change to the type of block to set
     * @return A mapping from world position to previous block type.
     * The value of a map entry is Null if the change failed (because the necessary chunk was not loaded)
     */
    default Map<Vector3ic, Block> setBlocks(Map<? extends Vector3ic, Block> blocks) {
        Map<Vector3ic, Block> resultMap = Maps.newHashMap();
        for (Map.Entry<? extends Vector3ic, Block> entry : blocks.entrySet()) {
            Block oldBlock = setBlock(entry.getKey(), entry.getValue());
            resultMap.put(entry.getKey(), oldBlock);
        }
        return resultMap;
    }

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
     * You must not use this method with world gen code, call 'setExtraData' on chunk instead.
     *
     * @param index The index of the extra data field
     * @param pos
     * @param value
     * @return The replaced value
     */
    int setExtraData(int index, Vector3ic pos, int value);

    /**
     * Disposes this world provider.
     */
    void dispose();

    WorldTime getTime();

    /**
     * @return an unmodifiable view on the generated relevant regions
     */
    Collection<BlockRegionc> getRelevantRegions();

}
