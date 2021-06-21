// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;


import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.protobuf.EntityData;
import org.terasology.gestalt.module.sandbox.API;

/**
 * Chunks are a box-shaped logical grouping of Terasology's blocks, for performance reasons.
 *
 * For example the renderer renders a single mesh for all opaque blocks in a chunk rather
 * than rendering each block as a separate mesh.
 *
 * For details on dimensions and other chunks characteristics see {@link Chunks}.
 */
@API
public interface Chunk extends RenderableChunk {

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
     * Sets type of block at given position relative to the chunk.
     *
     * @param x     X offset from the corner of the chunk
     * @param y     Y offset from the corner of the chunk
     * @param z     Z offset from the corner of the chunk
     * @param block Block to set block at given position to
     * @return Old Block at given position
     */
    Block setBlock(int x, int y, int z, Block block);

    /**
     * Sets type of block at given position relative to the chunk.
     *
     * @param pos   Position of the block relative to corner of the chunk
     * @param block Block to set block at given position to
     * @return Old Block at given position
     */
    Block setBlock(Vector3ic pos, Block block);

    /**
     * Sets one of the per-block custom data values at a given position relative to the chunk.
     * The given value is downcast from int to the appropriate type for the array. It is not
     * checked for overflow.
     *
     * @param index Index of the extra data array
     * @param x     X offset from the corner of the chunk
     * @param y     Y offset from the corner of the chunk
     * @param z     Z offset from the corner of the chunk
     * @param value New value to set the block to
     */
    void setExtraData(int index, int x, int y, int z, int value);

    /**
     * Sets one of the per-block custom data values at a given position relative to the chunk.
     * The given value is downcast from int to the appropriate type for the array. It is not
     * checked for overflow.
     *
     * @param index Index of the extra data array
     * @param pos   Position of the block relative to the corner of the chunk
     * @param value New value to set the block to
     */
    void setExtraData(int index, Vector3ic pos, int value);

    /**
     * Returns one of the per-block custom data values at a given position relative to the chunk.
     *
     * @param index Index of the extra data array
     * @param x     X offset from the corner of the chunk
     * @param y     Y offset from the corner of the chunk
     * @param z     Z offset from the corner of the chunk
     * @return Selected extra data value at the given location
     */
    int getExtraData(int index, int x, int y, int z);

    /**
     * Returns one of the per-block custom data values at a given position relative to the chunk.
     *
     * @param index Index of the extra data array
     * @param pos   Position of the block relative to the corner of the chunk
     * @return Selected extra data value at the given location
     */
    int getExtraData(int index, Vector3ic pos);


    /**
     * Returns offset of this chunk to the world center (0:0:0), with one unit being one block.
     *
     * @return Offset of this chunk from world center in blocks
     */
    Vector3i getChunkWorldOffset(Vector3i pos);

    default Vector3f getRenderPosition() {
        return new Vector3f(getChunkWorldOffsetX(), getChunkWorldOffsetY(), getChunkWorldOffsetZ());
    }


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
    /**
     * Returns the current amount of sunlight at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Current sunlight
     */
    byte getSunlight(Vector3ic pos);

    /**
     * Returns the current amount of sunlight at given position relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Current sunlight
     */
    byte getSunlight(int x, int y, int z);

    /**
     * Sets the amount of sunlight at given position relative to the chunk.
     *
     * @param pos    Position of the block relative to corner of the chunk
     * @param amount Amount of sunlight to set this block to
     * @return False if the amount is same as the old value, true otherwise
     */
    boolean setSunlight(Vector3ic pos, byte amount);

    /**
     * Sets the amount of sunlight at given position relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return False if the amount is same as the old value, true otherwise
     */
    boolean setSunlight(int x, int y, int z, byte amount);

    /**
     * Returns current value of sunlight regeneration for given block relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Current sunlight regeneration
     */
    byte getSunlightRegen(Vector3ic pos);

    /**
     * Returns current value of sunlight regeneration for given block relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Current sunlight regeneration
     */
    byte getSunlightRegen(int x, int y, int z);

    /**
     * Sets sunlight regeneration for given block relative to the chunk.
     *
     * @param pos    Position of the block relative to corner of the chunk
     * @param amount Sunlight regeneration amount
     * @return False if the amount is same as the old value, true otherwise
     */
    boolean setSunlightRegen(Vector3ic pos, byte amount);

    /**
     * Sets sunlight regeneration for given block relative to the chunk.
     *
     * @param x      X offset from the corner of the chunk
     * @param y      Y offset from the corner of the chunk
     * @param z      Z offset from the corner of the chunk
     * @param amount Sunlight regeneration amount
     * @return False if the amount is same as the old value, true otherwise
     */
    boolean setSunlightRegen(int x, int y, int z, byte amount);

    /**
     * Returns current amount of light for given block relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Current lightness
     */
    byte getLight(Vector3ic pos);

    /**
     * Returns current amount of light for given block relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Current lightness
     */
    byte getLight(int x, int y, int z);

    /**
     * Sets lightness for given block relative to the chunk.
     *
     * @param pos    Position of the block relative to corner of the chunk
     * @param amount Lightness value
     * @return False if the amount is same as the old value, true otherwise
     */
    boolean setLight(Vector3ic pos, byte amount);

    /**
     * Sets lightness for given block relative to the chunk.
     *
     * @param x      X offset from the corner of the chunk
     * @param y      Y offset from the corner of the chunk
     * @param z      Z offset from the corner of the chunk
     * @param amount Lightness value
     * @return False if the amount is same as the old value, true otherwise
     */
    boolean setLight(int x, int y, int z, byte amount);

    int getEstimatedMemoryConsumptionInBytes();

    ChunkBlockIterator getBlockIterator();

    void markReady();

    boolean isReady();

    void deflate();

    void deflateSunlight();

    void dispose();

    boolean isDisposed();

    void prepareForReactivation();

    // TODO: Expose appropriate iterators, remove this method
    EntityData.ChunkStore.Builder encode();

    boolean isDirty();

    void setDirty(boolean dirty);
}
