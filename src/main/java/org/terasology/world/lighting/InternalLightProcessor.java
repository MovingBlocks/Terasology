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

package org.terasology.world.lighting;

import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

/**
 * For doing an initial lighting sweep during chunk generation - bound to the chunk and assumed blank slate
 *
 * @author Immortius
 */
public class InternalLightProcessor {

    public static void generateInternalLighting(Chunk chunk) {
        int top = Chunk.SIZE_Y - 1;

        short[] tops = new short[Chunk.SIZE_X * Chunk.SIZE_Z];

        // Tunnel light down
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                Block lastBlock = BlockManager.getAir();
                int y = top;
                for (; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
                    if (LightingUtil.doesSunlightRetainsFullStrengthIn(block) && LightingUtil.canSpreadLightOutOf(lastBlock, Side.BOTTOM)
                            && LightingUtil.canSpreadLightInto(block, Side.TOP)) {
                        chunk.setSunlight(x, y, z, Chunk.MAX_LIGHT);
                        lastBlock = block;
                    } else {
                        break;
                    }
                }
                tops[x + Chunk.SIZE_X * z] = (short) y;
            }
        }

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                if (tops[x + Chunk.SIZE_X * z] < top) {
                    Block block = chunk.getBlock(x, tops[x + Chunk.SIZE_X * z] + 1, z);
                    spreadSunlightInternal(chunk, x, tops[x + Chunk.SIZE_X * z] + 1, z, block);
                }
                for (int y = top; y >= 0; y--) {
                    Block block = chunk.getBlock(x, y, z);
                    if (y > tops[x + Chunk.SIZE_X * z] && ((x > 0 && tops[(x - 1) + Chunk.SIZE_X * z] >= y) ||
                            (x < Chunk.SIZE_X - 1 && tops[(x + 1) + Chunk.SIZE_X * z] >= y) ||
                            (z > 0 && tops[x + Chunk.SIZE_X * (z - 1)] >= y) ||
                            (z < Chunk.SIZE_Z - 1 && tops[x + Chunk.SIZE_X * (z + 1)] >= y))) {
                        spreadSunlightInternal(chunk, x, y, z, block);
                    }
                    if (block.getLuminance() > 0) {
                        chunk.setLight(x, y, z, block.getLuminance());
                        spreadLightInternal(chunk, x, y, z, block);
                    }
                }
            }
        }
    }

    private static void spreadLightInternal(Chunk chunk, int x, int y, int z, Block block) {
        byte lightValue = chunk.getLight(x, y, z);
        if (lightValue <= 1) {
            return;
        }

        // TODO: use custom bounds checked iterator for this
        for (Side adjDir : Side.values()) {
            int adjX = x + adjDir.getVector3i().x;
            int adjY = y + adjDir.getVector3i().y;
            int adjZ = z + adjDir.getVector3i().z;
            if (chunk.isInBounds(adjX, adjY, adjZ)) {
                byte adjLightValue = chunk.getLight(adjX, adjY, adjZ);
                Block adjBlock = chunk.getBlock(adjX, adjY, adjZ);
                if (adjLightValue < lightValue - 1 && LightingUtil.canSpreadLightOutOf(block, adjDir) && LightingUtil.canSpreadLightInto(adjBlock, adjDir.reverse())) {
                    chunk.setLight(adjX, adjY, adjZ, (byte) (lightValue - 1));
                    spreadLightInternal(chunk, adjX, adjY, adjZ, adjBlock);
                }
            }
        }
    }

    private static void spreadSunlightInternal(Chunk chunk, int x, int y, int z, Block block) {
        byte lightValue = chunk.getSunlight(x, y, z);

        if (y > 0 && LightingUtil.canSpreadLightOutOf(block, Side.BOTTOM)) {
            Block adjBlock = chunk.getBlock(x, y - 1, z);
            if (chunk.getSunlight(x, y - 1, z) < lightValue - 1 && LightingUtil.canSpreadLightInto(adjBlock, Side.TOP)) {
                chunk.setSunlight(x, y - 1, z, (byte) (lightValue - 1));
                spreadSunlightInternal(chunk, x, y - 1, z, adjBlock);
            }
        }

        if (y < Chunk.SIZE_Y && lightValue < Chunk.MAX_LIGHT && LightingUtil.canSpreadLightOutOf(block, Side.TOP)) {
            Block adjBlock = chunk.getBlock(x, y + 1, z);
            if (chunk.getSunlight(x, y + 1, z) < lightValue - 1 && LightingUtil.canSpreadLightInto(adjBlock, Side.BOTTOM)) {
                chunk.setSunlight(x, y + 1, z, (byte) (lightValue - 1));
                spreadSunlightInternal(chunk, x, y + 1, z, adjBlock);
            }
        }

        if (lightValue <= 1) {
            return;
        }

        for (Side adjDir : Side.horizontalSides()) {
            int adjX = x + adjDir.getVector3i().x;
            int adjZ = z + adjDir.getVector3i().z;

            if (chunk.isInBounds(adjX, y, adjZ) && LightingUtil.canSpreadLightOutOf(block, adjDir)) {
                byte adjLightValue = chunk.getSunlight(adjX, y, adjZ);
                Block adjBlock = chunk.getBlock(adjX, y, adjZ);
                if (adjLightValue < lightValue - 1 && LightingUtil.canSpreadLightInto(adjBlock, adjDir.reverse())) {
                    chunk.setSunlight(adjX, y, adjZ, (byte) (lightValue - 1));
                    spreadSunlightInternal(chunk, adjX, y, adjZ, adjBlock);
                }
            }
        }
    }
}
