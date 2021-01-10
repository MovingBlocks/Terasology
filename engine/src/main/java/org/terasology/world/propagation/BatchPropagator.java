// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import org.joml.Math;
import org.joml.Vector3ic;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.LitChunk;

/**
 * Handles propagating values through blocks on a block by block basis.
 */
public interface BatchPropagator {

    /**
     * Handle one or more blocks being changed from one type to another
     *
     * @param changes The change being made
     */
    void process(BlockChange... changes);

    /**
     * Equivalent to {@link #process(BlockChange...)}
     *
     * @param blockChanges The changes to handle
     */
    void process(Iterable<BlockChange> blockChanges);

    /**
     * Propagate a value from one chunk to another
     *
     * @param chunk             The chunk the values are originating from
     * @param adjChunk          The chunk the values are moving into
     * @param side              The side the values are moving through
     * @param propagateExternal TODO: Document
     */
    void propagateBetween(LitChunk chunk, LitChunk adjChunk, Side side, boolean propagateExternal);

    /**
     * Propagates a value out of the block at the given position
     *
     * @param pos   The position of the block
     * @param block The block type
     */
    void propagateFrom(Vector3ic pos, Block block);

    @Deprecated
    default void propagateFrom(Vector3i pos, Block block) {
        propagateFrom(JomlUtil.from(pos), block);
    };

    /**
     * Propagates a specific value out from a location
     *
     * @param pos   The position to propagate out of
     * @param value The value to propagate out
     */
    void propagateFrom(Vector3ic pos, byte value);

    @Deprecated
    default void propagateFrom(Vector3i pos, byte value) {
        propagateFrom(JomlUtil.from(pos), value);
    }

    /**
     * TODO: Document
     *
     * @param pos   The position to regenerate at
     * @param value The original value at this position
     */
    void regenerate(Vector3ic pos, byte value);

    @Deprecated
    default void regenerate(Vector3i pos, byte value) {
        regenerate(JomlUtil.from(pos), value);
    }

    /**
     * Populates a target array with the minimum value adjacent to each location, including the location itself.
     *
     * @param source
     * @param target
     * @param populateMargins Whether to populate the edges of the target array
     */
    static void populateMinAdjacent2D(int[] source, int[] target, int dimX, int dimY, boolean populateMargins) {
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
