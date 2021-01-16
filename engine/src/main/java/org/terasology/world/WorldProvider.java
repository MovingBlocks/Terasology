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
package org.terasology.world;

import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.internal.WorldProviderCore;

/**
 * Provides the basic interface for all world providers.
 *
 */
public interface WorldProvider extends WorldProviderCore {

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param pos The position
     * @return Whether the given block is active
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #isBlockRelevant(Vector3ic)}.
     */
    @Deprecated
    default boolean isBlockRelevant(Vector3i pos) {
        return isBlockRelevant(JomlUtil.from(pos));
    };

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
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #isBlockRelevant(Vector3fc)}.
     */
    @Deprecated
    default boolean isBlockRelevant(Vector3f pos) {
        return isBlockRelevant(JomlUtil.from(pos));
    }

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
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #getBlock(Vector3fc)}.
     */
    @Deprecated
    default Block getBlock(Vector3f pos) {
        return getBlock(JomlUtil.from(pos));
    }

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
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #getBlock(Vector3ic)}.
     */
    @Deprecated
    default Block getBlock(Vector3i pos) {
        return getBlock(JomlUtil.from(pos));
    }

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

    @Deprecated
    default byte getLight(Vector3f pos) {
        return getLight(JomlUtil.from(pos));
    }

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3ic pos);

    default byte getLight(Vector3i pos) {
        return getLight(JomlUtil.from(pos));
    }

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3fc pos);

    @Deprecated
    default byte getSunlight(Vector3f pos) {
        return getSunlight(JomlUtil.from(pos));
    }

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3ic pos);

    @Deprecated
    default byte getSunlight(Vector3i pos) {
        return getSunlight(JomlUtil.from(pos));
    }

    byte getTotalLight(Vector3fc pos);

    @Deprecated
    default byte getTotalLight(Vector3f pos) {
        return getTotalLight(JomlUtil.from(pos));
    }

    byte getTotalLight(Vector3ic pos);

    @Deprecated
    default byte getTotalLight(Vector3i pos) {
        return getTotalLight(JomlUtil.from(pos));
    }

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

    @Deprecated
    default int getExtraData(int index, Vector3i pos) {
        return getExtraData(index, pos.x, pos.y, pos.z);
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

    @Deprecated
    default int getExtraData(String fieldName, Vector3i pos) {
        return getExtraData(fieldName, pos.x, pos.y, pos.z);
    }

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

    @Deprecated
    default int setExtraData(String fieldName, Vector3i pos, int value) {
        return setExtraData(fieldName, pos.x, pos.y, pos.z, value);
    }
}
