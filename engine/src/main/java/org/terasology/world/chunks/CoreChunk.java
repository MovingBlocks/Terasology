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
package org.terasology.world.chunks;

import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.StaticSound;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.Block;

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
     * @return Position of the chunk in world, where units of distance from origin are blocks
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getPosition(org.joml.Vector3i)}.
     */
    @Deprecated
    Vector3i getPosition();

    /**
     * Position of the chunk in world, where units of distance from origin are blocks
     *
     * @param dest will hold the result
     * @return dest
     */
    org.joml.Vector3i getPosition(org.joml.Vector3i dest);

    /**
     * Returns block at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Block at given position
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getBlock(Vector3ic)}.
     */
    @Deprecated
    Block getBlock(BaseVector3i pos);

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
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #setBlock(Vector3ic, Block)}.
     */
    @Deprecated
    Block setBlock(BaseVector3i pos, Block block);

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
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #setExtraData(int, Vector3ic, int)}.
     */
    @Deprecated
    void setExtraData(int index, BaseVector3i pos, int value);


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
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getExtraData(int, Vector3ic)}.
     */
    @Deprecated
    int getExtraData(int index, BaseVector3i pos);


    /**
     * Returns one of the per-block custom data values at a given position relative to the chunk.
     *
     * @param index Index of the extra data array
     * @param pos   Position of the block relative to the corner of the chunk
     * @return Selected extra data value at the given location
     */
    int getExtraData(int index, Vector3ic pos);

    /**
     * Returns offset of this chunk to the world center (0:0:0), with one unit being one chunk.
     *
     * @return Offset of this chunk from world center in chunks
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #getChunkWorldOffset(org.joml.Vector3i)}.
     */
    @Deprecated
    Vector3i getChunkWorldOffset();

    /**
     * Returns offset of this chunk to the world center (0:0:0), with one unit being one chunk.
     *
     * @return Offset of this chunk from world center in chunks
     */
    org.joml.Vector3i getChunkWorldOffset(org.joml.Vector3i pos);


    /**
     * Returns X offset of this chunk to the world center (0:0:0), with one unit being one chunk.
     *
     * @return X offset of this chunk from world center in chunks
     */
    int getChunkWorldOffsetX();

    /**
     * Returns Y offset of this chunk to the world center (0:0:0), with one unit being one chunk.
     *
     * @return Y offset of this chunk from world center in chunks
     */
    int getChunkWorldOffsetY();

    /**
     * Returns Z offset of this chunk to the world center (0:0:0), with one unit being one chunk.
     *
     * @return Z offset of this chunk from world center in chunks
     */
    int getChunkWorldOffsetZ();

    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param blockPos Position in this chunk you want to transform
     * @return Transformed position
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #chunkToWorldPosition(Vector3ic,org.joml.Vector3i)}.
     */
    @Deprecated
    Vector3i chunkToWorldPosition(BaseVector3i blockPos);

    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param dest Position in this chunk you want to transform
     * @return Transformed position
     */
    org.joml.Vector3i chunkToWorldPosition(Vector3ic blockPos, org.joml.Vector3i dest);


    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param x X position in this chunk you want to transform
     * @param y Y position in this chunk you want to transform
     * @param z Z position in this chunk you want to transform
     * @return Transformed position
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #chunkToWorldPosition(int, int, int, org.joml.Vector3i)}.
     */
    @Deprecated
    Vector3i chunkToWorldPosition(int x, int y, int z);


    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param x X position in this chunk you want to transform
     * @param y Y position in this chunk you want to transform
     * @param z Z position in this chunk you want to transform
     * @param dest will hold the result
     * @return dest
     */
    org.joml.Vector3i chunkToWorldPosition(int x, int y, int z, org.joml.Vector3i dest);


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
    Region3i getRegion();

    int getEstimatedMemoryConsumptionInBytes();

    ChunkBlockIterator getBlockIterator();
}
