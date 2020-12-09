// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks;

import org.joml.Math;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.TeraMath;
import org.terasology.module.sandbox.API;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegions;

@API
public final class Chunks {
    private Chunks() {
    }

    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 64;
    public static final int SIZE_Z = 32;

    public static final int INNER_CHUNK_POS_FILTER_X = TeraMath.ceilPowerOfTwo(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Y = TeraMath.ceilPowerOfTwo(SIZE_Y) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = TeraMath.ceilPowerOfTwo(SIZE_Z) - 1;

    public static final int POWER_X = Integer.highestOneBit(SIZE_X);
    public static final int POWER_Y = Integer.highestOneBit(SIZE_Y);
    public static final int POWER_Z = Integer.highestOneBit(SIZE_Z);

    public static final byte MAX_LIGHT = 0x0f; // max light for a light source 0-15
    public static final byte MAX_SUNLIGHT = 0x0f; // max sunlight for sunlight bounded 0-15
    public static final byte MAX_SUNLIGHT_REGEN = 63;
    public static final byte SUNLIGHT_REGEN_THRESHOLD = 48;

    public static final Vector3ic CHUNK_POWER = new Vector3i(POWER_X, POWER_Y, POWER_Z);
    public static final Vector3ic CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3ic INNER_CHUNK_POS_FILTER = new org.joml.Vector3i(INNER_CHUNK_POS_FILTER_X, INNER_CHUNK_POS_FILTER_Y, INNER_CHUNK_POS_FILTER_Z);
    public static final BlockRegion CHUNK_REGION = BlockRegions.createFromMinAndSize(new Vector3i(), CHUNK_SIZE);

    public static final Vector3ic LOCAL_REGION_EXTENTS = new Vector3i(1, 1, 1);

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param x The coordinate of the block
     * @param chunkPower the size of the chunk in powers of 2
     * @return The coordinate of the chunk
     */
    public static int toChunkPos(int x, int chunkPower) {
        return (x >> chunkPower);
    }

    public static int toChunkX(int x) {
        return toChunkPos(x, ChunkConstants.CHUNK_POWER.x);
    }

    public static int toChunkY(int y) {
        return toChunkPos(y, ChunkConstants.CHUNK_POWER.y);
    }

    public static int toChunkZ(int z) {
        return toChunkPos(z, ChunkConstants.CHUNK_POWER.z);
    }


    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * <p>default chunk size ({@link ChunkConstants#SIZE_X}, {@link ChunkConstants#SIZE_Y}, {@link
     * ChunkConstants#SIZE_Z}) </p>
     *
     * @param worldPos absolute position of the block
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toChunkPos(Vector3fc worldPos, Vector3i dest) {
        return toChunkPos(worldPos.x(), worldPos.y(), worldPos.z(), POWER_X, POWER_Y, POWER_Z, dest);
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
    public static Vector3i toChunkPos(float x, float y, float z, int chunkX, int chunkY, int chunkZ, Vector3i dest) {
        return toChunkPos(Math.roundUsing(x, org.joml.RoundingMode.FLOOR), Math.roundUsing(y, org.joml.RoundingMode.FLOOR), Math.roundUsing(z, org.joml.RoundingMode.FLOOR), chunkX, chunkY, chunkZ, dest);
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
    public static Vector3i toChunkPos(int x, int y, int z, int chunkX, int chunkY, int chunkZ, org.joml.Vector3i dest) {
        return dest.set(toChunkPos(x, chunkX), toChunkPos(y, chunkY), toChunkPos(z, chunkZ));
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
    public static BlockRegion toChunkRegion(BlockRegion region, int chunkX, int chunkY, int chunkZ, BlockRegion dest) {
        return dest.
            setMin(toChunkPos(region.getMinX(), chunkX), toChunkPos(region.getMinY(), chunkY), toChunkPos(region.getMinZ(), chunkZ)).
            setMax(toChunkPos(region.getMaxX(), chunkX), toChunkPos(region.getMaxY(), chunkY), toChunkPos(region.getMaxZ(), chunkZ));
    }

    /**
     * calculates a region that encasing a chunk
     *
     * @param region a bounding box that is contained
     * @param chunkPower  the size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static BlockRegion toChunkRegion(BlockRegion region, Vector3ic chunkPower, BlockRegion dest) {
        return toChunkRegion(region, chunkPower.x(), chunkPower.y(), chunkPower.z(), dest);
    }


    /**
     * calculates a region that encasing a chunk
     * This uses the default power ({@link ChunkConstants#POWER_X}, {@link ChunkConstants#POWER_Y}, {@link ChunkConstants#POWER_Z})
     *
     * @param region a bounding box that is contained
     * @param dest will hold the result
     * @return dest
     */
    public static BlockRegion toChunkRegion(BlockRegion region, BlockRegion dest) {
        return toChunkRegion(region, ChunkConstants.POWER_X, ChunkConstants.POWER_Y, ChunkConstants.POWER_Z, dest);
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param worldPos the world position of a block
     * @param filter the length of the chunk - 1
     * @return the relative position of the chunk
     */
    public static int toRelative(int worldPos, int filter) {
        return worldPos & filter;
    }


    /**
     * the relative position from the x axis from the (0,0,0) corner
     * @param blockX absolute x position
     * @return relative position for x axis
     */
    public static int toRelativeX(int blockX) {
        return toRelative(blockX, ChunkConstants.INNER_CHUNK_POS_FILTER.x());
    }

    /**
     * the relative position from the y axis from the (0,0,0) corner
     * @param blockY absolute y position
     * @return relative position for y axis
     */
    public static int toRelativeY(int blockY) {
        return toRelative(blockY, ChunkConstants.INNER_CHUNK_POS_FILTER.y());
    }

    /**
     * the relative position from the z axis from the (0,0,0) corner
     * @param blockZ absolute z position
     * @return relative position for z axis
     */
    public static int toRelativeZ(int blockZ) {
        return toRelative(blockZ, ChunkConstants.INNER_CHUNK_POS_FILTER.z());
    }
    /**
     * the relative position in the nearest chunk from the (0,0,0) corner
     * default chunk size of (32, 64, 32).
     * @param worldPos world position
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toRelative(Vector3ic worldPos, Vector3i dest) {
        return toRelative(worldPos.x(), worldPos.y(), worldPos.z(), INNER_CHUNK_POS_FILTER, dest);
    }

    /**
     * the relative position in the nearest chunk from the (0,0,0) corner.
     * default chunk size of (32, 64, 32).
     * @param x the x world position
     * @param y the y world position
     * @param z the z world position
     * @param chunkFilterSize relative within a chunk for (x - 1, y - 1, z - 1)
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toRelative(int x, int y, int z, Vector3ic chunkFilterSize, Vector3i dest) {
        return dest.set(toRelative(x, chunkFilterSize.x()), toRelative(y, chunkFilterSize.y()), toRelative(z, chunkFilterSize.z()));
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
     * @param chunkPos the chunk to check in
     * @return whether the block is inside the chunk
     */
    public static boolean inChunk(Vector3ic blockWorldPos, Vector3ic chunkPos) {
        return toChunkX(blockWorldPos.x()) == chunkPos.x()
            && toChunkY(blockWorldPos.y()) == chunkPos.y()
            && toChunkZ(blockWorldPos.z()) == chunkPos.z();
    }

}
