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
package org.terasology.world.propagation;

import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

/**
 * Rules to drive value propagation.
 *
 * @author Immortius
 */
public interface PropagationRules {

    /**
     * @param block
     * @return The value of provided by the given block
     */
    byte getBlockValue(Block block);

    /**
     * @param newBlock
     * @param oldBlock
     * @param side
     * @return The change in propagation switching from oldBlock to newBlock, on the given side
     */
    PropagationComparison comparePropagation(Block newBlock, Block oldBlock, Side side);

    /**
     * @param existingValue
     * @param side
     * @return The value propagate in the given direction from an existing value
     */
    byte propagateValue(byte existingValue, Side side);

    /**
     * @return The maximum value
     */
    byte getMaxValue();

    /**
     *
     * @param block
     * @param side
     * @return Whether the given block can propagated out through side
     */
    boolean canSpreadOutOf(Block block, Side side);

    /**
     * @param block
     * @param side
     * @return Whether the given block can be propagated into through side
     */
    boolean canSpreadInto(Block block, Side side);

    /**
     * @param chunk
     * @param pos
     * @return The value of the given position of a chunk
     */
    byte getValue(Chunk chunk, Vector3i pos);

    /**
     * Sets the value for a given chunk position
     * @param chunk
     * @param pos
     * @param value
     */
    void setValue(Chunk chunk, Vector3i pos, byte value);
}
