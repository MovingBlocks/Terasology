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

package org.terasology.math;

import java.math.RoundingMode;

import org.joml.Math;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.ChunkConstants;

/**
 * Collection of math functions.
 *
 */
public final class ChunkMath {

    private ChunkMath() {
    }

    /**
     * Returns the chunk position of a given coordinate.
     * @param x The coordinate of the block
     * @param chunkPower the size of the chunk in powers of 2
     * @return The coordinate of the chunk
     */
    public static int calcChunkPos(int x, int chunkPower) {
        return (x >> chunkPower);
    }

    public static int calcChunkPosX(int x) {
        return calcChunkPos(x, ChunkConstants.CHUNK_POWER.x);
    }

    public static int calcChunkPosY(int y) {
        return calcChunkPos(y, ChunkConstants.CHUNK_POWER.y);
    }

    public static int calcChunkPosZ(int z) {
        return calcChunkPos(z, ChunkConstants.CHUNK_POWER.z);
    }

    /**
     *
     * @param pos the absolute world position
     * @param chunkPower the location of the chunk
     * @return the relative block in the chunk
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkPos(Vector3ic, Vector3ic, org.joml.Vector3i)}.
     */
    @Deprecated
    public static Vector3i calcChunkPos(Vector3i pos, Vector3i chunkPower) {
        return calcChunkPos(pos.x, pos.y, pos.z, chunkPower);
    }

    /**
     *
     * @param pos
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkPos(Vector3fc, org.joml.Vector3i)}.
     */
    @Deprecated
    public static Vector3i calcChunkPos(Vector3f pos) {
        return calcChunkPos(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    /**
     *
     * @param pos
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkPos(Vector3ic, org.joml.Vector3i)}.
     */
    @Deprecated
    public static Vector3i calcChunkPos(Vector3i pos) {
        return calcChunkPos(pos.x, pos.y, pos.z);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkPos(int, int, int, org.joml.Vector3i)}.
     */
    @Deprecated
    public static Vector3i calcChunkPos(int x, int y, int z) {
        return calcChunkPos(x, y, z, ChunkConstants.CHUNK_POWER);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param chunkPower
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkPos(int, int, int, org.joml.Vector3i)}.
     */
    @Deprecated
    public static Vector3i calcChunkPos(int x, int y, int z, Vector3i chunkPower) {
        return new Vector3i(calcChunkPos(x, chunkPower.x), calcChunkPos(y, chunkPower.y), calcChunkPos(z, chunkPower.z));
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * <p>default chunk size ({@link ChunkConstants#SIZE_X}, {@link ChunkConstants#SIZE_Y}, {@link ChunkConstants#SIZE_Z}) </p>
     *
     * @param pos absolute position of the block
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(Vector3fc pos, org.joml.Vector3i dest) {
        return calcChunkPos(pos.x(), pos.y(), pos.z(), ChunkConstants.POWER_X, ChunkConstants.POWER_Y, ChunkConstants.POWER_Z, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * @param pos absolute position of the block
     * @param chunkPower the size of the chunk in powers 2
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(Vector3fc pos, Vector3ic chunkPower, org.joml.Vector3i dest) {
        return calcChunkPos(pos.x(), pos.y(), pos.z(), chunkPower.x(), chunkPower.y(), chunkPower.z(), dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power ({@link ChunkConstants#POWER_X}, {@link ChunkConstants#POWER_Y}, {@link ChunkConstants#POWER_Z})
     *
     * <p>default chunk size ({@link ChunkConstants#SIZE_X}, {@link ChunkConstants#SIZE_Y}, {@link ChunkConstants#SIZE_Z}) </p>
     *
     * @param pos absolute position of the block
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(Vector3ic pos, org.joml.Vector3i dest) {
        return calcChunkPos(pos.x(), pos.y(), pos.z(), ChunkConstants.POWER_X, ChunkConstants.POWER_Y, ChunkConstants.POWER_Z, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power ({@link ChunkConstants#POWER_X}, {@link ChunkConstants#POWER_Y}, {@link ChunkConstants#POWER_Z})
     *
     * <p>default chunk size ({@link ChunkConstants#SIZE_X}, {@link ChunkConstants#SIZE_Y}, {@link ChunkConstants#SIZE_Z}) </p>
     *
     * @param x absolute x coordinate of the block
     * @param y absolute y coordinate of the block
     * @param z absolute z coordinate of the block
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(int x, int y, int z, org.joml.Vector3i dest) {
        return calcChunkPos(x, y, z, ChunkConstants.POWER_X, ChunkConstants.POWER_Y, ChunkConstants.POWER_Z, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power ({@link ChunkConstants#POWER_X}, {@link ChunkConstants#POWER_Y}, {@link ChunkConstants#POWER_Z})
     *
     * <p>default chunk size ({@link ChunkConstants#SIZE_X}, {@link ChunkConstants#SIZE_Y}, {@link ChunkConstants#SIZE_Z}) </p>
     *
     * @param x absolute x coordinate of the block
     * @param y absolute y coordinate of the block
     * @param z absolute z coordinate of the block
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(float x, float y, float z, org.joml.Vector3i dest) {
        return calcChunkPos(x, y, z, ChunkConstants.POWER_X, ChunkConstants.POWER_Y, ChunkConstants.POWER_Z, dest);
    }


    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * @param pos the absolute position of the block
     * @param chunkPower the size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(Vector3ic pos, Vector3ic chunkPower, org.joml.Vector3i dest) {
        return calcChunkPos(pos.x(), pos.y(), pos.z(), chunkPower.x(), chunkPower.y(), chunkPower.z(), dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * @param x absolute x coordinate of the block
     * @param y absolute y coordinate of the block
     * @param z absolute z coordinate of the block
     * @param chunkPower  the size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(int x, int y, int z, Vector3ic chunkPower, org.joml.Vector3i dest) {
        return calcChunkPos(x, y, z, chunkPower.x(), chunkPower.y(), chunkPower.z(), dest);
    }

    /**
     * The position relative to the size of chunk with the given chunk power
     *
     * <p>Chunk size is in powers of 2 (2, 4, 8, 16, ...)</p>
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @param chunkX the x unit size of the chunk in powers of 2
     * @param chunkY the y unit size of the chunk in powers of 2
     * @param chunkZ the z unit size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(float x, float y, float z, int chunkX, int chunkY, int chunkZ, org.joml.Vector3i dest) {
        return calcChunkPos(Math.roundUsing(x, org.joml.RoundingMode.FLOOR), Math.roundUsing(y, org.joml.RoundingMode.FLOOR), Math.roundUsing(z, org.joml.RoundingMode.FLOOR), chunkX, chunkY, chunkZ, dest);
    }

    /**
     * The position relative to the size of chunk with the given chunk power
     *
     * <p>Chunk size is in powers of 2 (2, 4, 8, 16, ...)</p>
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @param chunkX the x unit size of the chunk in powers of 2
     * @param chunkY the y unit size of the chunk in powers of 2
     * @param chunkZ the z unit size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static org.joml.Vector3i calcChunkPos(int x, int y, int z, int chunkX, int chunkY, int chunkZ, org.joml.Vector3i dest) {
        return dest.set(calcChunkPos(x, chunkX), calcChunkPos(y, chunkY), calcChunkPos(z, chunkZ));
    }

    /**
     * calculates a region that encasing a chunk
     *
     * @param region a bounding box that is contained
     * @param chunkX the x unit size of the chunk in powers of 2
     * @param chunkY the y unit size of the chunk in powers of 2
     * @param chunkZ the z unit size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static BlockRegion calcChunkRegion(BlockRegion region, int chunkX, int chunkY, int chunkZ, BlockRegion dest) {
        return dest.
            setMin(calcChunkPos(region.getMinX(), chunkX), calcChunkPos(region.getMinY(), chunkY), calcChunkPos(region.getMinZ(), chunkZ)).
            setMax(calcChunkPos(region.getMaxX(), chunkX), calcChunkPos(region.getMaxY(), chunkY), calcChunkPos(region.getMaxZ(), chunkZ));
    }

    /**
     * calculates a region that encasing a chunk
     *
     * @param region a bounding box that is contained
     * @param chunkPower  the size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static BlockRegion calcChunkRegion(BlockRegion region, Vector3ic chunkPower, BlockRegion dest) {
        return calcChunkRegion(region, chunkPower.x(), chunkPower.y(), chunkPower.z(), dest);
    }

    /**
     * calculates a region that encasing a chunk
     * This uses the default power ({@link ChunkConstants#POWER_X}, {@link ChunkConstants#POWER_Y}, {@link ChunkConstants#POWER_Z})
     *
     * @param region a bounding box that is contained
     * @param dest will hold the result
     * @return dest
     */
    public static BlockRegion calcChunkRegion(BlockRegion region, BlockRegion dest) {
        return calcChunkRegion(region, ChunkConstants.POWER_X, ChunkConstants.POWER_Y, ChunkConstants.POWER_Z, dest);
    }

    /**
     *
     * @param region
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkRegion(BlockRegion, BlockRegion)}.
     */
    public static Vector3i[] calcChunkPos(Region3i region) {
        return calcChunkPos(region, ChunkConstants.CHUNK_POWER);
    }


    /**
     *
     * @param region
     * @param chunkPower
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #calcChunkRegion(BlockRegion,Vector3ic, BlockRegion)}.
     */
    public static Vector3i[] calcChunkPos(Region3i region, Vector3i chunkPower) {
        int minX = calcChunkPos(region.minX(), chunkPower.x);
        int minY = calcChunkPos(region.minY(), chunkPower.y);
        int minZ = calcChunkPos(region.minZ(), chunkPower.z);

        int maxX = calcChunkPos(region.maxX(), chunkPower.x);
        int maxY = calcChunkPos(region.maxY(), chunkPower.y);
        int maxZ = calcChunkPos(region.maxZ(), chunkPower.z);

        int size = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

        Vector3i[] result = new Vector3i[size];
        int index = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    result[index++] = new Vector3i(x, y, z);
                }
            }
        }
        return result;
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param pos the world position of the chunk
     * @param filter the length of the chunk - 1
     * @return the relative position of the chunk
     */
    public static int calcRelativeBlockPos(int pos, int filter) {
        return pos & filter;
    }


    public static int calcBlockPosX(int blockX) {
        return calcRelativeBlockPos(blockX, ChunkConstants.INNER_CHUNK_POS_FILTER.x);
    }

    public static int calcBlockPosY(int blockY) {
        return calcRelativeBlockPos(blockY, ChunkConstants.INNER_CHUNK_POS_FILTER.y);
    }

    public static int calcBlockPosZ(int blockZ) {
        return calcRelativeBlockPos(blockZ, ChunkConstants.INNER_CHUNK_POS_FILTER.z);
    }

    public static Vector3i calcRelativeBlockPos(Vector3i worldPos) {
        return calcRelativeBlockPos(worldPos.x, worldPos.y, worldPos.z, ChunkConstants.INNER_CHUNK_POS_FILTER);
    }

    public static Vector3i calcRelativeBlockPos(int x, int y, int z) {
        return calcRelativeBlockPos(x, y, z, ChunkConstants.INNER_CHUNK_POS_FILTER);
    }

    public static Vector3i calcRelativeBlockPos(int x, int y, int z, Vector3i chunkFilterSize) {
        return new Vector3i(calcRelativeBlockPos(x, chunkFilterSize.x), calcRelativeBlockPos(y, chunkFilterSize.y), calcRelativeBlockPos(z, chunkFilterSize.z));
    }

    public static Region3i getChunkRegionAroundWorldPos(Vector3i pos, int extent) {
        Vector3i minPos = new Vector3i(-extent, -extent, -extent);
        minPos.add(pos);
        Vector3i maxPos = new Vector3i(extent, extent, extent);
        maxPos.add(pos);

        Vector3i minChunk = calcChunkPos(minPos);
        Vector3i maxChunk = calcChunkPos(maxPos);

        return Region3i.createFromMinMax(minChunk, maxChunk);
    }

    // TODO: This doesn't belong in this class, move it.
    public static Side getSecondaryPlacementDirection(Vector3f direction, Vector3f normal) {
        Side surfaceDir = Side.inDirection(normal);
        Vector3f attachDir = surfaceDir.reverse().getVector3i().toVector3f();
        Vector3f rawDirection = new Vector3f(direction);
        float dot = rawDirection.dot(attachDir);
        rawDirection.sub(new Vector3f(dot * attachDir.x, dot * attachDir.y, dot * attachDir.z));
        return Side.inDirection(rawDirection.x, rawDirection.y, rawDirection.z).reverse();
    }

    /**
     * Produces a region containing the region touching the side of the given region, both in and outside the region.
     *
     * @param region
     * @param side
     * @return
     */
    public static Region3i getEdgeRegion(Region3i region, Side side) {
        Vector3i sideDir = side.getVector3i();
        Vector3i min = region.min();
        Vector3i max = region.max();
        Vector3i edgeMin = new Vector3i(min);
        Vector3i edgeMax = new Vector3i(max);
        if (sideDir.x < 0) {
            edgeMin.x = min.x;
            edgeMax.x = min.x;
        } else if (sideDir.x > 0) {
            edgeMin.x = max.x;
            edgeMax.x = max.x;
        } else if (sideDir.y < 0) {
            edgeMin.y = min.y;
            edgeMax.y = min.y;
        } else if (sideDir.y > 0) {
            edgeMin.y = max.y;
            edgeMax.y = max.y;
        } else if (sideDir.z < 0) {
            edgeMin.z = min.z;
            edgeMax.z = min.z;
        } else if (sideDir.z > 0) {
            edgeMin.z = max.z;
            edgeMax.z = max.z;
        }
        return Region3i.createFromMinMax(edgeMin, edgeMax);
    }

    /**
     * Populates a target array with the minimum value adjacent to each location, including the location itself. TODO:
     * this is too specific for a general class like this. Move to a new class AbstractBatchPropagator
     *
     * @param source
     * @param target
     * @param populateMargins Whether to populate the edges of the target array
     */
    public static void populateMinAdjacent2D(int[] source, int[] target, int dimX, int dimY, boolean populateMargins) {
        System.arraycopy(source, 0, target, 0, target.length);

        // 0 < x < dimX - 1; 0 < y < dimY - 1
        for (int y = 1; y < dimY - 1; ++y) {
            for (int x = 1; x < dimX - 1; ++x) {
                target[x + y * dimX] = Math.min(Math.min(source[x + (y - 1) * dimX], source[x + (y + 1) * dimX]),
                    Math.min(source[x + 1 + y * dimX], source[x - 1 + y * dimX]));
            }
        }

        if (populateMargins) {
            // x == 0, y == 0
            target[0] = Math.min(source[1], source[dimX]);

            // 0 < x < dimX - 1, y == 0
            for (int x = 1; x < dimX - 1; ++x) {
                target[x] = Math.min(source[x - 1], Math.min(source[x + 1], source[x + dimX]));
            }

            // x == dimX - 1, y == 0
            target[dimX - 1] = Math.min(source[2 * dimX - 1], source[dimX - 2]);

            // 0 < y < dimY - 1
            for (int y = 1; y < dimY - 1; ++y) {
                // x == 0
                target[y * dimX] = Math.min(source[dimX * (y - 1)], Math.min(source[dimX * (y + 1)], source[1 + dimX * y]));
                // x == dimX - 1
                target[dimX - 1 + y * dimX] = Math.min(source[dimX - 1 + dimX * (y - 1)], Math.min(source[dimX - 1 + dimX * (y + 1)], source[dimX - 2 + dimX * y]));
            }
            // x == 0, y == dimY - 1
            target[dimX * (dimY - 1)] = Math.min(source[1 + dimX * (dimY - 1)], source[dimX * (dimY - 2)]);

            // 0 < x < dimX - 1; y == dimY - 1
            for (int x = 1; x < dimX - 1; ++x) {
                target[x + dimX * (dimY - 1)] = Math.min(source[x - 1 + dimX * (dimY - 1)], Math.min(source[x + 1 + dimX * (dimY - 1)], source[x + dimX * (dimY - 2)]));
            }

            // x == dimX - 1; y == dimY - 1
            target[dimX - 1 + dimX * (dimY - 1)] = Math.min(source[dimX - 2 + dimX * (dimY - 1)], source[dimX - 1 + dimX * (dimY - 2)]);
        }
    }

    /**
     * Works out whether the given block resides inside the given chunk.
     * <p>
     * Both positions must be given as world position, not local position. In addition, the chunk position must be given
     * in chunk coordinates, not in block coordinates.
     * <p>
     * For example, using chunks of width 32, a block with x coordinate of 33 will be counted as inside a chunk with x
     * coordinate of 1.
     *
     * @param blockWorldPos the block to check for
     * @param chunkWorldPos the chunk to check in
     * @return whether the block is inside the chunk
     */
    public static boolean blockInChunk(Vector3i blockWorldPos, Vector3i chunkWorldPos) {
        return calcChunkPos(blockWorldPos).equals(chunkWorldPos);
    }
}
