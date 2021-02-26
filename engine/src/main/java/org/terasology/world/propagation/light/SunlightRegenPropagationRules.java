// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation.light;

import org.joml.Vector3ic;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.propagation.PropagationComparison;

/**
 * Defines and interfaces how sunlight values regenerate per block
 */
public class SunlightRegenPropagationRules extends CommonLightPropagationRules {

    /**
     * Sunlight has no fixed value per block
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getFixedValue(Block block, Vector3ic pos) {
        return 0;
    }

    /**
     * Sunlight goes to zero unless leaving via the bottom face.
     * In that case it increases up until the maximum value in {@link Chunks#MAX_SUNLIGHT_REGEN}
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte propagateValue(byte existingValue, Side side, Block from, int scale) {
        if (side == Side.BOTTOM) {
            return (byte) Math.min(Chunks.MAX_SUNLIGHT_REGEN, existingValue + scale);
        }
        return 0;
    }

    /**
     * The maximum value of sunlight is given by {@link Chunks#MAX_SUNLIGHT_REGEN}
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getMaxValue() {
        return Chunks.MAX_SUNLIGHT_REGEN;
    }

    @Override
    public byte getValue(LitChunk chunk, Vector3ic pos) {
        return getValue(chunk, pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getValue(LitChunk chunk, int x, int y, int z) {
        return chunk.getSunlightRegen(x, y, z);
    }

    @Override
    public void setValue(LitChunk chunk, Vector3ic pos, byte value) {
        chunk.setSunlightRegen(pos, value);
    }

    /**
     * In all non-vertical sides the propagation is unchanged
     * <p>
     * {@inheritDoc}
     */
    @Override
    public PropagationComparison comparePropagation(Block newBlock, Block oldBlock, Side side) {
        if (!side.isVertical()) {
            return PropagationComparison.IDENTICAL;
        }
        return super.comparePropagation(newBlock, oldBlock, side);
    }

    /**
     * Sunlight can only spread out of the bottom of a non-liquid block
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean canSpreadOutOf(Block block, Side side) {
        return side == Side.BOTTOM && !block.isLiquid() && (super.canSpreadOutOf(block, side));
    }

    /**
     * Sunlight can spread if the block is not a liquid
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean canSpreadInto(Block block, Side side) {
        return !block.isLiquid() && super.canSpreadInto(block, side);
    }
}
