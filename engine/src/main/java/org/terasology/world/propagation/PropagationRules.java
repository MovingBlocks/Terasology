// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import org.joml.Vector3ic;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.LitChunk;

/**
 * Rules to drive value propagation.
 */
public interface PropagationRules {

    /**
     * Gets the constant values for this block, that are unaffected by the light propagation rules
     * @param block The block being checked
     * @param pos The position of the block being checked
     * @return The constant value of this block
     */
    byte getFixedValue(Block block, Vector3ic pos);

    /**
     * Compare the how the propagation changes if you replace the block with a different one, on a given side
     *
     * @param newBlock The new block
     * @param oldBlock THe original block
     * @param side     The side to check on
     * @return How the propagation rules change
     */
    PropagationComparison comparePropagation(Block newBlock, Block oldBlock, Side side);

    /**
     * Get the new value after propagating in a specified manner
     *
     * @param existingValue The value to propagate
     * @param side          The side the value is leaving by
     * @param from          The block the value is leaving
     * @param scale         The scale of the chunk
     * @return The new value to set at the block position
     */
    byte propagateValue(byte existingValue, Side side, Block from, int scale);

    /**
     * @return The maximum value possible for this data
     */
    byte getMaxValue();

    /**
     * Checks if the value can leave a given block, through a specific side
     *
     * @param block The block to leave
     * @param side  The side to leave by
     * @return Whether the given block can propagated out through side
     */
    boolean canSpreadOutOf(Block block, Side side);

    /**
     * Checks if the value can propagate into a given block, through a specific side
     *
     * @param block The block to propagate into
     * @param side  The side to propagate via
     * @return Whether the given block can be propagated
     * into through side
     */
    boolean canSpreadInto(Block block, Side side);

    /**
     * Get the value of a given position
     *
     * @param chunk The chunk the position is in
     * @param pos   The position to get from
     * @return The value of the given position of a chunk
     */
    byte getValue(LitChunk chunk, Vector3ic pos);

    /**
     * See {@link #getValue(LitChunk, Vector3ic)}
     *
     * @param chunk The chunk the position is in
     * @param x     The x position
     * @param y     The y position
     * @param z     THe z position
     * @return The value at the posiiton int he given chunk
     */
    byte getValue(LitChunk chunk, int x, int y, int z);

    /**
     * Sets the value for a given chunk position
     *
     * @param chunk The chunk the position is in
     * @param pos   The position to set at
     * @param value The value to set to
     */
    void setValue(LitChunk chunk, Vector3ic pos, byte value);
}
