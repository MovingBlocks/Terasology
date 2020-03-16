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
import org.joml.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.PropagationRules;
import org.terasology.world.propagation.SingleChunkView;
import org.terasology.world.propagation.StandardBatchPropagator;

/**
 * For doing an initial lighting sweep during chunk generation - bound to the chunk and assumed blank slate
 * Sets up the values for the subsequent stages of propagation
 */
public final class InternalLightProcessor {

    private static final PropagationRules LIGHT_RULES = new LightPropagationRules();
    private static final PropagationRules SUNLIGHT_REGEN_RULES = new SunlightRegenPropagationRules();

    private InternalLightProcessor() {
    }

    public static void generateInternalLighting(LitChunk chunk) {
        populateSunlightRegen(chunk);
        populateSunlight(chunk);
        populateLight(chunk);
    }

    /**
     * Propagate out light from the initial luminous blocks
     *
     * @param chunk The chunk to populate through
     */
    private static void populateLight(LitChunk chunk) {
        BatchPropagator lightPropagator = new StandardBatchPropagator(LIGHT_RULES, new SingleChunkView(LIGHT_RULES, chunk));
        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                for (int y = 0; y < ChunkConstants.SIZE_Y; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getLuminance() > 0) {
                        chunk.setLight(x, y, z, block.getLuminance());
                        lightPropagator.propagateFrom(new Vector3i(x, y, z), block.getLuminance());
                    }
                }
            }
        }
        lightPropagator.process();
    }

    /**
     * Propagate the initial sunlight values out
     *
     * @param chunk The chunk to set in
     */
    private static void populateSunlight(LitChunk chunk) {
        PropagationRules sunlightRules = new SunlightPropagationRules(chunk);
        BatchPropagator lightPropagator = new StandardBatchPropagator(sunlightRules, new SingleChunkView(sunlightRules, chunk));

        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                /* Start at the bottom of the chunk and then move up until the max sunlight level */
                for (int y = 0; y < ChunkConstants.MAX_SUNLIGHT; y++) {
                    Vector3i pos = new Vector3i(x, y, z);
                    Block block = chunk.getBlock(x, y, z);
                    byte light = sunlightRules.getFixedValue(block, pos);
                    if (light > 0) {
                        chunk.setSunlight(x, y, z, light);
                        lightPropagator.propagateFrom(pos, light);
                    }
                }
            }
        }
        lightPropagator.process();
    }

    /**
     * Sets the initial values for the sunlight regeneration
     *
     * @param chunk The chunk to populate the regeneration values through
     */
    private static void populateSunlightRegen(LitChunk chunk) {
        int top = ChunkConstants.SIZE_Y - 1;
        /* Scan through each column in the chunk & propagate light from the top down */
        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                byte regen = 0;
                Block lastBlock = chunk.getBlock(x, top, z);
                for (int y = top - 1; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
                    /* If the regeneration can propagate down into this block */
                    if (SUNLIGHT_REGEN_RULES.canSpreadOutOf(lastBlock, Side.BOTTOM) && SUNLIGHT_REGEN_RULES.canSpreadInto(block, Side.TOP)) {
                        regen = SUNLIGHT_REGEN_RULES.propagateValue(regen, Side.BOTTOM, lastBlock);
                        chunk.setSunlightRegen(x, y, z, regen);
                    } else {
                        regen = 0;
                    }
                    lastBlock = block;
                }
            }
        }
    }
}
