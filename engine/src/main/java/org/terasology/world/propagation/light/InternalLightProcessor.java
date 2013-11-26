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
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.PropagationRules;
import org.terasology.world.propagation.SingleChunkView;

/**
 * For doing an initial lighting sweep during chunk generation - bound to the chunk and assumed blank slate
 *
 * @author Immortius
 */
public final class InternalLightProcessor {

    private static final PropagationRules LIGHT_RULES = new LightPropagationRules();
    private static final PropagationRules SUNLIGHT_RULES = new SunlightPropagationRules();

    private InternalLightProcessor() {
    }

    public static void generateInternalLighting(ChunkImpl chunk) {
        int top = ChunkConstants.SIZE_Y - 1;

        short[] tops = new short[ChunkConstants.SIZE_X * ChunkConstants.SIZE_Z];

        byte sunlightMax = SUNLIGHT_RULES.getMaxValue();

        // Tunnel light down
        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                Block lastBlock = BlockManager.getAir();
                int y = top;
                for (; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
                    if (SUNLIGHT_RULES.propagateValue(sunlightMax, Side.BOTTOM, lastBlock) == sunlightMax
                            && SUNLIGHT_RULES.canSpreadOutOf(lastBlock, Side.BOTTOM) && SUNLIGHT_RULES.canSpreadInto(block, Side.TOP)) {
                        chunk.setSunlight(x, y, z, sunlightMax);
                        lastBlock = block;
                    } else {
                        break;
                    }
                }
                tops[x + ChunkConstants.SIZE_X * z] = (short) y;
            }
        }

        BatchPropagator lightPropagator = new BatchPropagator(LIGHT_RULES, new SingleChunkView(LIGHT_RULES, chunk));
        for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                if (tops[x + ChunkConstants.SIZE_X * z] < top) {
                    Block block = chunk.getBlock(x, tops[x + ChunkConstants.SIZE_X * z] + 1, z);
                    spreadSunlightInternal(chunk, x, tops[x + ChunkConstants.SIZE_X * z] + 1, z, block);
                }
                for (int y = top; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
                    if (y > tops[x + ChunkConstants.SIZE_X * z] && ((x > 0 && tops[(x - 1) + ChunkConstants.SIZE_X * z] >= y)
                            || (x < ChunkConstants.SIZE_X - 1 && tops[(x + 1) + ChunkConstants.SIZE_X * z] >= y)
                            || (z > 0 && tops[x + ChunkConstants.SIZE_X * (z - 1)] >= y)
                            || (z < ChunkConstants.SIZE_Z - 1 && tops[x + ChunkConstants.SIZE_X * (z + 1)] >= y))) {
                        spreadSunlightInternal(chunk, x, y, z, block);
                    }
                    if (block.getLuminance() > 1) {
                        lightPropagator.propagateFrom(new Vector3i(x, y, z), block);
                    }
                }
            }
        }

        lightPropagator.process();
    }

    private static void spreadSunlightInternal(ChunkImpl chunk, int x, int y, int z, Block block) {
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
    }
}
