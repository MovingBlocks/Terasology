// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.internal.WorldProviderCore;

/**
 * Provides the basic interface for all world providers.
 *
 */
public interface WorldProvider extends WorldProviderCore {

    /**
     * An active block is in a chunk that is available and fully generated.
     * @param pos The position
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(Vector3ic pos);

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param pos The position
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(Vector3fc pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(Vector3fc pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    default Block getBlock(Vector3ic pos) {
        return getBlock(pos.x(), pos.y(), pos.z());
    }

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3fc pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3ic pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3fc pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3ic pos);

    byte getTotalLight(Vector3ic pos);

    byte getTotalLight(Vector3fc pos);

    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param index The index of the extra data field
     * @param pos
     * @return The (index)th extra-data value at the given position
     */
    default int getExtraData(int index, Vector3ic pos) {
        return getExtraData(index, pos.x(), pos.y(), pos.z());
    }

    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param fieldName The name of the extra-data field
     * @param x
     * @param y
     * @param z
     * @return The named extra-data value at the given position
     */
    int getExtraData(String fieldName, int x, int y, int z);

    /**
     * Gets one of the per-block custom data values at the given position. Returns 0 outside the view.
     *
     * @param fieldName The name of the extra-data field
     * @param pos
     * @return The named extra-data value at the given position
     */
    default int getExtraData(String fieldName, Vector3ic pos) {
        return getExtraData(fieldName, pos.x(), pos.y(), pos.z());
    }

    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     * You must not use this method with world gen code, call 'setExtraData' on chunk instead.
     *
     * @param index The index of the extra data field
     * @param x
     * @param y
     * @param z
     * @param value
     * @return The replaced value
     */
    int setExtraData(int index, int x, int y, int z, int value);

    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     * You must not use this method with world gen code, call 'setExtraData' on chunk instead.
     *
     * @param fieldName The name of the extra-data field
     * @param x
     * @param y
     * @param z
     * @param value
     * @return The replaced value
     */
    int setExtraData(String fieldName, int x, int y, int z, int value);

    /**
     * Sets one of the per-block custom data values at the given position, if it is within the view.
     * You must not use this method with world gen code, call 'setExtraData' on chunk instead.
     *
     * @param fieldName The name of the extra-data field
     * @param pos
     * @param value
     * @return The replaced value
     */
    default int setExtraData(String fieldName, Vector3ic pos, int value) {
        return setExtraData(fieldName, pos.x(), pos.y(), pos.z(), value);
    }
}
