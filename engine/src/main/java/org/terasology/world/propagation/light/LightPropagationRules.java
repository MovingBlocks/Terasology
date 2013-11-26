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
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.internal.ChunkImpl;

/**
 * @author Immortius
 */
public class LightPropagationRules extends CommonLightPropagationRules {

    @Override
    public byte getBlockValue(Block block) {
        return block.getLuminance();
    }

    @Override
    public byte propagateValue(byte existingValue, Side side, Block from) {
        return (byte) (existingValue - 1);
    }

    @Override
    public byte getMaxValue() {
        return (byte) 15;
    }

    @Override
    public byte getValue(ChunkImpl chunk, Vector3i pos) {
        return chunk.getLight(pos);
    }

    @Override
    public void setValue(ChunkImpl chunk, Vector3i pos, byte value) {
        chunk.setLight(pos, value);
    }


}
