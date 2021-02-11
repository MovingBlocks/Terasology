// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.chunks;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegionc;

/**
 * This interface describes the core of a chunk:
 * <ul>
 * <li>Chunk position</li>
 * <li>Block read/write</li>
 * <li>Extra data read/write</li>
 * <li>Chunk to world position conversion</li>
 * <li>Chunk size and region</li>
 * <li>Locking</li>
 * </ul>
 * This is everything available during chunk generation. Light is not included because it is a derived from the blocks.
 */
@API
public interface CoreChunk {

    /**
     * @return Position of the chunk in world, where units of distance from origin are chunks
     */
    Vector3ic getPosition();

    /**
     * Position of the chunk in world, where units of distance from origin are chunks
     *
     * @param dest will hold the result
     * @return dest
     * @deprecated use {@link #getPosition()}
     */
    @Deprecated
    org.joml.Vector3i getPosition(org.joml.Vector3i dest);

    /**
     * Returns block at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Block at given position
     */
    Block getBlock(Vector3ic pos);

    /**
     * Returns block at given position relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Block at given position
     */
    Block getBlock(int x, int y, int z);

    /**
     * Returns block's id at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Block at given position
     */
    short getBlockId(Vector3ic pos);

    /**
     * Returns block's id at given position relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Block at given position
     */
    short getBlockId(int x, int y, int z);

    /**
     * Sets type of block at given position relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @param block Block to set block at given position to
     * @return Old Block at given position
     */
    Block setBlock(int x, int y, int z, Block block);

    /**
     * Sets type of block at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @param block Block to set block at given position to
     * @return Old Block at given position
     */
    Block setBlock(Vector3ic pos, Block block);

    /**
     * Sets one of the per-block custom data values at a given position relative to the chunk. The given value is
     * downcast from int to the appropriate type for the array. It is not checked for overflow.
     *
     * @param index Index of the extra data array
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @param value New value to set the block to
     */
    void setExtraData(int index, int x, int y, int z, int value);

    /**
     * Sets one of the per-block custom data values at a given position relative to the chunk. The given value is
     * downcast from int to the appropriate type for the array. It is not checked for overflow.
     *
     * @param index Index of the extra data array
     * @param pos Position of the block relative to the corner of the chunk
     * @param value New value to set the block to
     */
    void setExtraData(int index, Vector3ic pos, int value);

    /**
     * Returns one of the per-block custom data values at a given position relative to the chunk.
     *
     * @param index Index of the extra data array
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Selected extra data value at the given location
     */
    int getExtraData(int index, int x, int y, int z);

    /**
     * Returns one of the per-block custom data values at a given position relative to the chunk.
     *
     * @param index Index of the extra data array
     * @param pos Position of the block relative to the corner of the chunk
     * @return Selected extra data value at the given location
     */
    int getExtraData(int index, Vector3ic pos);


    /**
     * Returns offset of this chunk to the world center (0:0:0), with one unit being one block.
     *
     * @return Offset of this chunk from world center in blocks
     */
    Vector3i getChunkWorldOffset(Vector3i pos);


    /**
     * Returns X offset of this chunk to the world center (0:0:0), with one unit being one block.
     *
     * @return X offset of this chunk from world center in blocks
     */
    int getChunkWorldOffsetX();

    /**
     * Returns Y offset of this chunk to the world center (0:0:0), with one unit being one block.
     *
     * @return Y offset of this chunk from world center in blocks
     */
    int getChunkWorldOffsetY();

    /**
     * Returns Z offset of this chunk to the world center (0:0:0), with one unit being one block.
     *
     * @return Z offset of this chunk from world center in blocks
     */
    int getChunkWorldOffsetZ();


    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param dest Position in this chunk you want to transform
     * @return Transformed position
     */
    Vector3i chunkToWorldPosition(Vector3ic blockPos, Vector3i dest);


    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param x X position in this chunk you want to transform
     * @param y Y position in this chunk you want to transform
     * @param z Z position in this chunk you want to transform
     * @param dest will hold the result
     * @return dest
     */
    Vector3i chunkToWorldPosition(int x, int y, int z, Vector3i dest);


    /**
     * Returns X position in this chunk transformed to world coordinate.
     *
     * @param x X  position in this chunk you want to transform
     * @return Transformed position
     */
    int chunkToWorldPositionX(int x);

    /**
     * Returns Y position in this chunk transformed to world coordinate.
     *
     * @param y Y  position in this chunk you want to transform
     * @return Transformed position
     */
    int chunkToWorldPositionY(int y);

    /**
     * Returns Z position in this chunk transformed to world coordinate.
     *
     * @param z Z  position in this chunk you want to transform
     * @return Transformed position
     */
    int chunkToWorldPositionZ(int z);

    /**
     * @return Size of the chunk along the X axis.
     */
    int getChunkSizeX();

    /**
     * @return Size of the chunk along the Y axis.
     */
    int getChunkSizeY();

    /**
     * @return Size of the chunk along the Z axis.
     */
    int getChunkSizeZ();

    /**
     * @return Chunk's Region
     */
    BlockRegionc getRegion();

    int getEstimatedMemoryConsumptionInBytes();

    ChunkBlockIterator getBlockIterator();
}
