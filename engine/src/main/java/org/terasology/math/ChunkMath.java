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

import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
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
     *
     * @param x The X-coordinate of the block
     * @return The X-coordinate of the chunk
     */
    public static int calcChunkPosX(int x, int chunkPowerX) {
        return (x >> chunkPowerX);
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param y The Y-coordinate of the block
     * @return The Y-coordinate of the chunk
     */
    public static int calcChunkPosY(int y, int chunkPowerY) {
        return (y >> chunkPowerY);
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param z The Z-coordinate of the block
     * @return The Z-coordinate of the chunk
     */
    public static int calcChunkPosZ(int z, int chunkPowerZ) {
        return (z >> chunkPowerZ);
    }

    public static Vector3i calcChunkPos(Vector3i pos, Vector3i chunkPower) {
        return calcChunkPos(pos.x, pos.y, pos.z, chunkPower);
    }

    public static Vector3i calcChunkPos(Vector3f pos) {
        return calcChunkPos(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    public static Vector3i calcChunkPos(Vector3i pos) {
        return calcChunkPos(pos.x, pos.y, pos.z);
    }

    public static Vector3i calcChunkPos(int x, int y, int z) {
        return calcChunkPos(x, y, z, ChunkConstants.CHUNK_POWER);
    }

    public static Vector3i[] calcChunkPos(Region3i region) {
        return calcChunkPos(region, ChunkConstants.CHUNK_POWER);
    }

    public static Vector3i calcChunkPos(int x, int y, int z, Vector3i chunkPower) {
        return new Vector3i(calcChunkPosX(x, chunkPower.x), calcChunkPosY(y, chunkPower.y), calcChunkPosZ(z, chunkPower.z));
    }

    public static Vector3i[] calcChunkPos(Region3i region, Vector3i chunkPower) {
        int minX = calcChunkPosX(region.minX(), chunkPower.x);
        int minY = calcChunkPosY(region.minY(), chunkPower.y);
        int minZ = calcChunkPosZ(region.minZ(), chunkPower.z);

        int maxX = calcChunkPosX(region.maxX(), chunkPower.x);
        int maxY = calcChunkPosY(region.maxY(), chunkPower.y);
        int maxZ = calcChunkPosZ(region.maxZ(), chunkPower.z);

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
     * @param blockX The X-coordinate of the block in the world
     * @return The X-coordinate of the block within the chunk
     */
    public static int calcBlockPosX(int blockX, int chunkPosFilterX) {
        return blockX & chunkPosFilterX;
    }

    public static int calcBlockPosY(int blockY, int chunkPosFilterY) {
        return blockY & chunkPosFilterY;
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param blockZ The Z-coordinate of the block in the world
     * @return The Z-coordinate of the block within the chunk
     */
    public static int calcBlockPosZ(int blockZ, int chunkPosFilterZ) {
        return blockZ & chunkPosFilterZ;
    }

    public static Vector3i calcBlockPos(Vector3i worldPos) {
        return calcBlockPos(worldPos.x, worldPos.y, worldPos.z, ChunkConstants.INNER_CHUNK_POS_FILTER);
    }

    public static Vector3i calcBlockPos(int x, int y, int z) {
        return calcBlockPos(x, y, z, ChunkConstants.INNER_CHUNK_POS_FILTER);
    }

    public static Vector3i calcBlockPos(int x, int y, int z, Vector3i chunkFilterSize) {
        return new Vector3i(calcBlockPosX(x, chunkFilterSize.x), calcBlockPosY(y, chunkFilterSize.y), calcBlockPosZ(z, chunkFilterSize.z));
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
     * Populates a target array with the minimum value adjacent to each location, including the location itself.
     * TODO: this is too specific for a general class like this. Move to a new class AbstractBatchPropagator
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
}
