// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.joml.Vector3ic;
import org.terasology.module.sandbox.API;

/**
 * This interface describes the light properties of a chunk:
 * <ul>
 * <li>Sunlight</li>
 * <li>Sunlight regeneration</li>
 * <li>Light</li>
 * </ul>
 */
@API
public interface LitChunk extends CoreChunk {
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
}
