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

import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.sandbox.API;
import org.terasology.world.biomes.Biome;
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
     */
    Vector3i getPosition();

    /**
     * Returns block at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Block at given position
     */
    Block getBlock(BaseVector3i pos);

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
    Block setBlock(BaseVector3i pos, Block block);

    /**
     * Sets biome at given position relative to the chunk.
     *
     * @param x     X offset from the corner of the chunk
     * @param y     Y offset from the corner of the chunk
     * @param z     Z offset from the corner of the chunk
     * @param biome Biome to set block at given position to
     * @return Old Biome at given position
     */
    Biome setBiome(int x, int y, int z, Biome biome);

    /**
     * Sets biome at given position relative to the chunk.
     *
     * @param pos   Position of the block relative to corner of the chunk
     * @param biome Biome to set block at given position to
     * @return Old Biome at given position
     */
    Biome setBiome(BaseVector3i pos, Biome biome);

    /**
     * Returns Biome at given position relative to the chunk.
     *
     * @param x X offset from the corner of the chunk
     * @param y Y offset from the corner of the chunk
     * @param z Z offset from the corner of the chunk
     * @return Biome at given position
     */
    Biome getBiome(int x, int y, int z);

    /**
     * Returns Biome at given position relative to the chunk.
     *
     * @param pos Position of the block relative to corner of the chunk
     * @return Biome at given position
     */
    Biome getBiome(BaseVector3i pos);

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
    void setExtraData(int index, BaseVector3i pos, int value);
    
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
    int getExtraData(int index, BaseVector3i pos);

    /**
     * Returns offset of this chunk to the world center (0:0:0), with one unit being one chunk.
     *
     * @return Offset of this chunk from world center in chunks
     */
    Vector3i getChunkWorldOffset();

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
     */
    Vector3i chunkToWorldPosition(BaseVector3i blockPos);

    /**
     * Returns position in this chunk transformed to world coordinates.
     *
     * @param x X position in this chunk you want to transform
     * @param y Y position in this chunk you want to transform
     * @param z Z position in this chunk you want to transform
     * @return Transformed position
     */
    Vector3i chunkToWorldPosition(int x, int y, int z);

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
