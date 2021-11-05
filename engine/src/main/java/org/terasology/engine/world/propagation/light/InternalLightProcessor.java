// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.propagation.light;

import org.joml.Vector3i;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.propagation.BatchPropagator;
import org.terasology.engine.world.propagation.PropagationRules;
import org.terasology.engine.world.propagation.SingleChunkView;
import org.terasology.engine.world.propagation.StandardBatchPropagator;

/**
 * For doing an initial lighting sweep during chunk generation - bound to the chunk and assumed blank slate
 * Sets up the values for the subsequent stages of propagation
 */
public final class InternalLightProcessor {

    private static final PropagationRules LIGHT_RULES = new LightPropagationRules();
    private static final PropagationRules SUNLIGHT_REGEN_RULES = new SunlightRegenPropagationRules();

    private InternalLightProcessor() {
    }

    public static void generateInternalLighting(Chunk chunk) {
        generateInternalLighting(chunk, 1);
    }

    public static void generateInternalLighting(Chunk chunk, int scale) {
        populateSunlightRegen(chunk, scale);
        populateSunlight(chunk, scale);
        populateLight(chunk, scale);
    }

    /**
     * Propagate out light from the initial luminous blocks
     *
     * @param chunk The chunk to populate through
     */
    private static void populateLight(Chunk chunk, int scale) {
        BatchPropagator lightPropagator = new StandardBatchPropagator(LIGHT_RULES, new SingleChunkView(LIGHT_RULES, chunk), scale);
        Vector3i pos = new Vector3i();
        for (int x = 0; x < Chunks.SIZE_X; x++) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                for (int y = 0; y < Chunks.SIZE_Y; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getLuminance() > 0) {
                        chunk.setLight(x, y, z, block.getLuminance());
                        lightPropagator.propagateFrom(pos.set(x, y, z), block.getLuminance());
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
    private static void populateSunlight(Chunk chunk, int scale) {
        PropagationRules sunlightRules = new SunlightPropagationRules(chunk);
        BatchPropagator lightPropagator = new StandardBatchPropagator(sunlightRules, new SingleChunkView(sunlightRules, chunk), scale);

        Vector3i pos = new Vector3i();
        for (int x = 0; x < Chunks.SIZE_X; x++) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                /* Start at the bottom of the chunk and then move up until the max sunlight level */
                for (int y = 0; y < Chunks.SIZE_Y; y++) {
                    pos.set(x, y, z);
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
    private static void populateSunlightRegen(Chunk chunk, int scale) {
        int top = Chunks.SIZE_Y - 1;
        /* Scan through each column in the chunk & propagate light from the top down */
        for (int x = 0; x < Chunks.SIZE_X; x++) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                byte regen = chunk.getSunlightRegen(x, top, z);
                Block lastBlock = chunk.getBlock(x, top, z);
                for (int y = top - 1; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
                    /* If the regeneration can propagate down into this block */
                    if (SUNLIGHT_REGEN_RULES.canSpreadOutOf(lastBlock, Side.BOTTOM) && SUNLIGHT_REGEN_RULES.canSpreadInto(block, Side.TOP)) {
                        regen = SUNLIGHT_REGEN_RULES.propagateValue(regen, Side.BOTTOM, lastBlock, scale);
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
