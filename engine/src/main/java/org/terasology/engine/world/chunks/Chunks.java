// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks;

import org.joml.Math;
import org.joml.RoundingMode;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.context.annotation.API;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;

@API
public final class Chunks {
    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 64;
    public static final int SIZE_Z = 32;

    public static final int INNER_CHUNK_POS_FILTER_X = Integer.highestOneBit(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Y = Integer.highestOneBit(SIZE_Y) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = Integer.highestOneBit(SIZE_Z) - 1;

    public static final int POWER_X = Integer.numberOfTrailingZeros(SIZE_X);
    public static final int POWER_Y = Integer.numberOfTrailingZeros(SIZE_Y);
    public static final int POWER_Z = Integer.numberOfTrailingZeros(SIZE_Z);

    public static final byte MAX_LIGHT = 0x0f; // max light for a light source 0-15
    public static final byte MAX_SUNLIGHT = 0x0f; // max sunlight for sunlight bounded 0-15
    public static final byte MAX_SUNLIGHT_REGEN = 63;
    public static final byte SUNLIGHT_REGEN_THRESHOLD = 48;

    public static final Vector3ic CHUNK_POWER = new Vector3i(POWER_X, POWER_Y, POWER_Z);
    public static final Vector3ic CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3ic INNER_CHUNK_POS_FILTER =
            new Vector3i(INNER_CHUNK_POS_FILTER_X, INNER_CHUNK_POS_FILTER_Y, INNER_CHUNK_POS_FILTER_Z);
    public static final BlockRegionc CHUNK_REGION = new BlockRegion(0, 0, 0).setSize(CHUNK_SIZE);

    public static final Vector3ic LOCAL_REGION_EXTENTS = new Vector3i(1, 1, 1);

    private Chunks() {
    }

    //-- chunk position ----------------------------------------------------------------------------------------------//

    /**
     * Returns the chunk coordinate given the position and the chunk power.
     *
     * @param val The coordinate of the block
     * @param chunkPower the size of the chunk in powers of 2
     * @return The coordinate of the chunk
     */
    public static int toChunkPos(int val, int chunkPower) {
        return (val >> chunkPower);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power {@link #POWER_X}.
     *
     * @param x the x component
     * @return The coordinate of the chunk
     */
    public static int toChunkPosX(int x) {
        return toChunkPos(x, CHUNK_POWER.x());
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power {@link #POWER_Y}
     *
     * @param y the y component
     * @return The coordinate of the chunk
     */
    public static int toChunkPosY(int y) {
        return toChunkPos(y, CHUNK_POWER.y());
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power {@link #POWER_Z}
     *
     * @param z the z component
     * @return The coordinate of the chunk
     */
    public static int toChunkPosZ(int z) {
        return toChunkPos(z, CHUNK_POWER.z());
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * <p>default chunk size ({@link Chunks#SIZE_X}, {@link Chunks#SIZE_Y}, {@link
     * Chunks#SIZE_Z}) </p>
     *
     * @param worldPos absolute position of the block
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toChunkPos(Vector3fc worldPos, Vector3i dest) {
        return toChunkPos(worldPos.x(), worldPos.y(), worldPos.z(), POWER_X, POWER_Y, POWER_Z, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power ({@link #POWER_X}, {@link #POWER_Y}, {@link #POWER_Z})
     *
     * <p>default chunk size ({@link #SIZE_X}, {@link #SIZE_Y}, {@link #SIZE_Z}) </p>
     *
     * @param x absolute x coordinate of the block
     * @param y absolute y coordinate of the block
     * @param z absolute z coordinate of the block
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toChunkPos(int x, int y, int z, Vector3i dest) {
        return toChunkPos(x, y, z, POWER_X, POWER_Y, POWER_Z, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     * This uses the default power ({@link #POWER_X}, {@link #POWER_Y}, {@link #POWER_Z})
     *
     * <p>default chunk size ({@link #SIZE_X}, {@link #SIZE_Y}, {@link #SIZE_Z}) </p>
     *
     * @param pos absolute coordinate of the block
     * @param dest will hold the result
     * @return dest
     *
     * @see #toChunkPos(Vector3i)
     */
    public static Vector3i toChunkPos(Vector3ic pos, Vector3i dest) {
        return toChunkPos(pos.x(), pos.y(), pos.z(), POWER_X, POWER_Y, POWER_Z, dest);
    }

    /**
     * Compute (in-place) the position of the chunk given the world coordinate {@code pos}.
     *
     * This uses the default power ({@link #POWER_X}, {@link #POWER_Y}, {@link #POWER_Z})
     *
     * <p>default chunk size ({@link #SIZE_X}, {@link #SIZE_Y}, {@link #SIZE_Z}) </p>
     *
     * @param pos absolute coordinate of the block
     * @return the input vector {@code pos} modified to hold the result
     */
    public static Vector3i toChunkPos(Vector3i pos) {
        return toChunkPos(pos, pos);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
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
        return toChunkPos(
            Math.roundUsing(x, RoundingMode.FLOOR),
            Math.roundUsing(y, RoundingMode.FLOOR),
            Math.roundUsing(z, RoundingMode.FLOOR), chunkX, chunkY, chunkZ, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
     *
     * <p>Chunk size is in powers of 2 (2, 4, 8, 16, ...)</p>
     *
     * @param x the x coordinate of the block
     * @param y the y coordinate of the block
     * @param z the z coordinate of the block
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toChunkPos(float x, float y, float z, Vector3i dest) {
        return toChunkPos(
            Math.roundUsing(x, RoundingMode.FLOOR),
            Math.roundUsing(y, RoundingMode.FLOOR),
            Math.roundUsing(z, RoundingMode.FLOOR), POWER_X, POWER_Y, POWER_Z, dest);
    }

    /**
     * The position of the chunk given the coordinate and size of chunk in powers of 2.
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
    public static Vector3i toChunkPos(int x, int y, int z, int chunkX, int chunkY, int chunkZ, Vector3i dest) {
        return dest.set(
            toChunkPos(x, chunkX),
            toChunkPos(y, chunkY),
            toChunkPos(z, chunkZ));
    }

    //-- chunk region ------------------------------------------------------------------------------------------------//

    /**
     * Maps a {@link BlockRegion} to the chunks that intersect the {@link BlockRegion}.
     *
     * @param region a bounding box that is contained
     * @param chunkX the x unit size of the chunk in powers of 2
     * @param chunkY the y unit size of the chunk in powers of 2
     * @param chunkZ the z unit size of the chunk in powers of 2
     * @param dest will hold the result
     * @return dest
     */
    public static BlockRegion toChunkRegion(BlockRegionc region, int chunkX, int chunkY, int chunkZ, BlockRegion dest) {
        return dest.
            set(toChunkPos(region.minX(), chunkX), toChunkPos(region.minY(), chunkY), toChunkPos(region.minZ(), chunkZ),
                toChunkPos(region.maxX(), chunkX), toChunkPos(region.maxY(), chunkY), toChunkPos(region.maxZ(), chunkZ));
    }

    /**
     * Maps a {@link BlockRegion} to the relative chunks that intersect the {@link BlockRegion}.
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
     * Maps a {@link BlockRegion} to the chunks that intersect the {@link BlockRegion}.
     * This uses the default power ({@link Chunks#POWER_X}, {@link Chunks#POWER_Y}, {@link Chunks#POWER_Z})
     *
     * @param region a bounding box that is contained
     * @param dest will hold the result
     * @return dest
     * @see #toChunkRegion(BlockRegion)
     */
    public static BlockRegion toChunkRegion(BlockRegionc region, BlockRegion dest) {
        return toChunkRegion(region, Chunks.POWER_X, Chunks.POWER_Y, Chunks.POWER_Z, dest);
    }

    /**
     * Maps the {@link BlockRegion} in-place to the region of chunks it intersects.
     *
     * This uses the default power ({@link Chunks#POWER_X}, {@link Chunks#POWER_Y}, {@link Chunks#POWER_Z})
     *
     * @param region a bounding box that is contained
     * @return the in-place modified {@code region}
     */
    public static BlockRegion toChunkRegion(BlockRegion region) {
        return toChunkRegion(region, region);
    }

    //-- chunk-relative position -------------------------------------------------------------------------------------//

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
     * the relative position from the x axis from the (0,0,0) corner.
     *
     * @param blockX absolute x position
     * @return relative position for x axis
     */
    public static int toRelativeX(int blockX) {
        return toRelative(blockX, Chunks.INNER_CHUNK_POS_FILTER.x());
    }

    /**
     * the relative position from the y axis from the (0,0,0) corner.
     *
     * @param blockY absolute y position
     * @return relative position for y axis
     */
    public static int toRelativeY(int blockY) {
        return toRelative(blockY, Chunks.INNER_CHUNK_POS_FILTER.y());
    }

    /**
     * the relative position from the z axis from the (0,0,0) corner.
     * @param blockZ absolute z position
     * @return relative position for z axis
     */
    public static int toRelativeZ(int blockZ) {
        return toRelative(blockZ, Chunks.INNER_CHUNK_POS_FILTER.z());
    }

    /**
     * the relative position in the nearest chunk from the (0,0,0) corner.
     * Default chunk size of ({@link #SIZE_X}, {@link #SIZE_Y}, {@link #SIZE_Z}).
     *
     * @param worldPos world position
     * @param dest will hold the result
     * @return dest
     * @see #toRelative(Vector3i)
     */
    public static Vector3i toRelative(Vector3ic worldPos, Vector3i dest) {
        return toRelative(worldPos.x(), worldPos.y(), worldPos.z(), INNER_CHUNK_POS_FILTER, dest);
    }

    /**
     * Compute (in-place) the relative position in the nearest chunk from the (0,0,0) corner.
     *
     * Default chunk size of ({@link #SIZE_X}, {@link #SIZE_Y}, {@link #SIZE_Z}).
     *
     * @param worldPos world position
     * @return the modified {@code worldPos} vector
     */
    public static Vector3i toRelative(Vector3i worldPos) {
        return toRelative(worldPos, worldPos);
    }

    /**
     * the relative position in the nearest chunk from the (0,0,0) corner.
     *
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
     * the relative position in the nearest chunk from the (0,0,0) corner.
     * Default chunk size of ({@link #SIZE_X}, {@link #SIZE_Y}, {@link #SIZE_Z}).
     *
     * @param x the x world position
     * @param y the y world position
     * @param z the z world position
     * @param dest will hold the result
     * @return dest
     */
    public static Vector3i toRelative(int x, int y, int z, Vector3i dest) {
        return dest.set(toRelative(x, INNER_CHUNK_POS_FILTER.x()),
                toRelative(y, INNER_CHUNK_POS_FILTER.y()),
                toRelative(z, INNER_CHUNK_POS_FILTER.z()));
    }

    //-- checks ------------------------------------------------------------------------------------------------------//

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
        return toChunkPos(blockWorldPos.x(), POWER_X) == chunkPos.x()
            && toChunkPos(blockWorldPos.y(), POWER_Y) == chunkPos.y()
            && toChunkPos(blockWorldPos.z(), POWER_Z) == chunkPos.z();
    }
}
