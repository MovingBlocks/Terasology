/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.world.propagation.PropagationComparison;

/**
 */
public class SunlightRegenPropagationRules extends CommonLightPropagationRules {

    @Override
    public byte getFixedValue(Block block, Vector3i pos) {
        return 0;
    }

    @Override
    public byte propagateValue(byte existingValue, Side side, Block from) {
        if (side == Side.BOTTOM) {
            return (existingValue == ChunkConstants.MAX_SUNLIGHT_REGEN) ? existingValue : (byte) (existingValue + 1);
        }
        return 0;
    }

    @Override
    public byte getMaxValue() {
        return ChunkConstants.MAX_SUNLIGHT_REGEN;
    }

    @Override
    public byte getValue(LitChunk chunk, Vector3i pos) {
        return getValue(chunk, pos.x, pos.y, pos.z);
    }

    @Override
    public byte getValue(LitChunk chunk, int x, int y, int z) {
        return chunk.getSunlightRegen(x, y, z);
    }

    @Override
    public void setValue(LitChunk chunk, Vector3i pos, byte value) {
        chunk.setSunlightRegen(pos, value);
    }

    @Override
    public PropagationComparison comparePropagation(Block newBlock, Block oldBlock, Side side) {
        if (!side.isVertical()) {
            return PropagationComparison.IDENTICAL;
        }
        return super.comparePropagation(newBlock, oldBlock, side);
    }

    @Override
    public boolean canSpreadOutOf(Block block, Side side) {
        return side == Side.BOTTOM && !block.isLiquid() && (super.canSpreadOutOf(block, side));
    }

    @Override
    public boolean canSpreadInto(Block block, Side side) {
        return !block.isLiquid() && super.canSpreadInto(block, side);
    }
}
