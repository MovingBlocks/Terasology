/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;

/**
 * Provides the basic interface for all world providers.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface WorldProvider extends WorldProviderCore {

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param pos
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(Vector3i pos);

    boolean isBlockRelevant(Vector3f pos);

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     * <p/>
     * This method takes the expected value of the previous block in this position - this allows it to check the block
     * hasn't been changed (potentially by another thread). If it has changed then no change occurs. It is recommended
     * that this is used to ensure that the block being changed is in an acceptable state for the change.
     *
     * @param pos     Block position
     * @param type    The type of the block to set
     * @param oldType The expected type of the block being replaced.
     * @return True if a block was set/replaced. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    boolean setBlock(Vector3i pos, Block type, Block oldType);

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     * <p/>
     * This method forces the change regardless of the previous value. It should generally be avoided except in situations where
     * the change must absolutely be forced.
     *
     * @param pos  Block position
     * @param type The type of the block to set
     * @return True if a block was set/replaced. Will fail of oldType != the current type, or if the underlying chunk is not available
     */
    void setBlockForced(Vector3i pos, Block type);

    /**
     * @param pos
     * @param state    The new value of the liquid state
     * @param oldState The expected previous value of the liquid state
     * @return Whether the liquid change was made successfully. Will fail of oldState != the current state, or if the underlying chunk is not available
     */
    boolean setLiquid(Vector3i pos, LiquidData state, LiquidData oldState);

    /**
     * Returns the liquid state at the given position.
     *
     * @param blockPos
     * @return The state of the block
     */
    LiquidData getLiquid(Vector3i blockPos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(Vector3f pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(Vector3i pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3f pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3f pos);

    byte getTotalLight(Vector3f pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3i pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3i pos);

    byte getTotalLight(Vector3i pos);

    float getFog(Vector3f pos);

}
