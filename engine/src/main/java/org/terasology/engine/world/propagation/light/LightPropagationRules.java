// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import org.joml.Vector3ic;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.LitChunk;

/**
 * Rules for how standard light should propagate.
 * Also provides implementation for setting and getting values
 */
public class LightPropagationRules extends CommonLightPropagationRules {

    /**
     * Any luminance from the block is a constant
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getFixedValue(Block block, Vector3ic pos) {
        return block.getLuminance();
    }

    /**
     * When the light propagates it's light level reduces by one
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte propagateValue(byte existingValue, Side side, Block from, int scale) {
        return (byte) Math.max(existingValue - scale, 0);
    }

    /**
     * The maximum light level is a full byte of 15
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getMaxValue() {
        return Chunks.MAX_LIGHT; // 15
    }

    @Override
    public byte getValue(LitChunk chunk, Vector3ic pos) {
        return getValue(chunk, pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getValue(LitChunk chunk, int x, int y, int z) {
        return chunk.getLight(x, y, z);
    }

    @Override
    public void setValue(LitChunk chunk, Vector3ic pos, byte value) {
        chunk.setLight(pos, value);
    }


}
