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
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.PropagationRules;
import org.terasology.world.propagation.SingleChunkView;
import org.terasology.world.propagation.StandardBatchPropagator;

/**
 * For doing an initial lighting sweep during chunk generation - bound to the chunk and assumed blank slate
 *
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

    private static void populateSunlight(LitChunk chunk) {
        PropagationRules sunlightRules = new SunlightPropagationRules(chunk);
        BatchPropagator lightPropagator = new StandardBatchPropagator(sunlightRules, new SingleChunkView(sunlightRules, chunk));

        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                for (int y = 0; y < ChunkConstants.MAX_SUNLIGHT; ++y) {
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

    private static void populateSunlightRegen(LitChunk chunk) {
        int top = ChunkConstants.SIZE_Y - 1;
        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                int y = top;
                byte regen = 0;
                Block lastBlock = chunk.getBlock(x, y, z);
                for (y -= 1; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
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

    /*private static void spreadSunlightInternal(ChunkImpl chunk, int x, int y, int z, Block block) {
        byte lightValue = chunk.getSunlight(x, y, z);

        if (lightValue <= 1) {
            return;
        }

        if (y > 0 && SUNLIGHT_RULES.canSpreadOutOf(block, Side.BOTTOM)) {
            Block adjBlock = chunk.getBlock(x, y - 1, z);
            if (chunk.getSunlight(x, y - 1, z) < lightValue - 1 && SUNLIGHT_RULES.canSpreadInto(adjBlock, Side.TOP)) {
                chunk.setSunlight(x, y - 1, z, (byte) (lightValue - 1));
                spreadSunlightInternal(chunk, x, y - 1, z, adjBlock);
            }
        }

        if (y < ChunkConstants.SIZE_Y && lightValue < ChunkConstants.MAX_LIGHT && SUNLIGHT_RULES.canSpreadOutOf(block, Side.TOP)) {
            Block adjBlock = chunk.getBlock(x, y + 1, z);
            if (chunk.getSunlight(x, y + 1, z) < lightValue - 1 && SUNLIGHT_RULES.canSpreadInto(adjBlock, Side.BOTTOM)) {
                chunk.setSunlight(x, y + 1, z, (byte) (lightValue - 1));
                spreadSunlightInternal(chunk, x, y + 1, z, adjBlock);
            }
        }

        for (Side adjDir : Side.horizontalSides()) {
            int adjX = x + adjDir.getVector3i().x;
            int adjZ = z + adjDir.getVector3i().z;

            if (chunk.isInBounds(adjX, y, adjZ) && SUNLIGHT_RULES.canSpreadOutOf(block, adjDir)) {
                byte adjLightValue = chunk.getSunlight(adjX, y, adjZ);
                Block adjBlock = chunk.getBlock(adjX, y, adjZ);
                if (adjLightValue < lightValue - 1 && SUNLIGHT_RULES.canSpreadInto(adjBlock, adjDir.reverse())) {
                    chunk.setSunlight(adjX, y, adjZ, (byte) (lightValue - 1));
                    spreadSunlightInternal(chunk, adjX, y, adjZ, adjBlock);
                }
            }
        }
    } */
}
