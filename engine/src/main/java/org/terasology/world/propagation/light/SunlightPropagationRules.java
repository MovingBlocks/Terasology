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
package org.terasology.world.propagation.light;

import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.propagation.PropagatorWorldView;
import org.terasology.world.propagation.SingleChunkView;

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
    public byte getFixedValue(Block block, Vector3i pos) {
        byte lightVal = (byte) (regenWorldView.getValueAt(pos) - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD);
        return (lightVal > 0) ? lightVal : 0;
    }

    /**
     * Sunlight reduces by one to a minimum of zero per propagation
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte propagateValue(byte existingValue, Side side, Block from) {
        return (byte) Math.max(existingValue - 1, 0);
    }

    /**
     * The maximum sunlight is given by {@link ChunkConstants#MAX_SUNLIGHT}
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getMaxValue() {
        return ChunkConstants.MAX_SUNLIGHT;
    }

    @Override
    public byte getValue(LitChunk chunk, Vector3i pos) {
        return getValue(chunk, pos.x, pos.y, pos.z);
    }

    @Override
    public byte getValue(LitChunk chunk, int x, int y, int z) {
        return chunk.getSunlight(x, y, z);
    }

    @Override
    public void setValue(LitChunk chunk, Vector3i pos, byte value) {
        chunk.setSunlight(pos, value);
    }

}
