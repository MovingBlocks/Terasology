// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import org.joml.Vector3ic;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.LitChunk;
import org.terasology.engine.world.propagation.PropagatorWorldView;
import org.terasology.engine.world.propagation.SingleChunkView;

/**
 * Rules that determine how the sunlight propagates
 */
public class SunlightPropagationRules extends CommonLightPropagationRules {

    private PropagatorWorldView regenWorldView;

    public SunlightPropagationRules(PropagatorWorldView regenWorldView) {
        this.regenWorldView = regenWorldView;
    }

    public SunlightPropagationRules(LitChunk chunk) {
        this.regenWorldView = new SingleChunkView(new SunlightRegenPropagationRules(), chunk);
    }

    /**
     * If the light is above the sunlight regeneration threshold it is maintained, otherwise it is zero
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getFixedValue(Block block, Vector3ic pos) {
        byte lightVal = (byte) (regenWorldView.getValueAt(pos) - Chunks.SUNLIGHT_REGEN_THRESHOLD);
        return (lightVal > 0) ? lightVal : 0;
    }

    /**
     * Sunlight reduces by one to a minimum of zero per propagation
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte propagateValue(byte existingValue, Side side, Block from, int scale) {
        return (byte) Math.max(existingValue - scale, 0);
    }

    /**
     * The maximum sunlight is given by {@link Chunks#MAX_SUNLIGHT}
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getMaxValue() {
        return Chunks.MAX_SUNLIGHT;
    }

    @Override
    public byte getValue(LitChunk chunk, Vector3ic pos) {
        return getValue(chunk, pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getValue(LitChunk chunk, int x, int y, int z) {
        return chunk.getSunlight(x, y, z);
    }

    @Override
    public void setValue(LitChunk chunk, Vector3ic pos, byte value) {
        chunk.setSunlight(pos, value);
    }

}
