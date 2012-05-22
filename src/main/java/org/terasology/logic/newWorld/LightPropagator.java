/*
 * Copyright 2012
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

package org.terasology.logic.newWorld;

import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

/**
 * @author Immortius
 */
public class LightPropagator {

    private static final Vector3i[] HORIZONTAL_LIGHT_DIRECTIONS = {
            new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
            new Vector3i(0, 0, 1), new Vector3i(0, 0, -1)
    };

    private WorldView worldView;

    public LightPropagator(WorldView worldView) {
        this.worldView = worldView;
    }

    public void propagateOutOfTargetChunk() {
        int maxX = NewChunk.CHUNK_DIMENSION_X - 1;
        int maxZ = NewChunk.CHUNK_DIMENSION_Z - 1;
        // Iterate over the blocks on the horizontal sides
        for (int y = 0; y < NewChunk.CHUNK_DIMENSION_Y; y++) {
            for (int x = 0; x < maxX; x++) {
                propagateFrom(x, y, 0);
                propagateFrom(x + 1, y, maxZ);
            }
            for (int z = 0; z < maxZ; z++) {
                propagateFrom(0, y, z + 1);
                propagateFrom(maxX, y, z);
            }
        }
    }

    private void propagateFrom(int blockX, int blockY, int blockZ) {
        propagateSunlightFrom(blockX, blockY, blockZ);
        //propagateLightFrom(blockX, blockY, blockZ);
    }

    private void propagateSunlightFrom(int blockX, int blockY, int blockZ) {
        byte lightLevel = worldView.getSunlight(blockX, blockY, blockZ);
        if (lightLevel == 0) return;
        // Propagate down
        if (blockY > 0 && worldView.getBlock(blockX, blockY - 1, blockZ).isTranslucent()) {
            byte lowerLight = worldView.getSunlight(blockX, blockY - 1, blockZ);
            if (lightLevel == NewChunk.MAX_LIGHT) {
                if (lowerLight < lightLevel) {
                    worldView.setSunlight(blockX, blockY - 1, blockZ, lightLevel);
                    propagateSunlightFrom(blockX, blockY - 1, blockZ);
                }
            } else {
                if (lowerLight < lightLevel - 1) {
                    worldView.setSunlight(blockX, blockY - 1, blockZ, (byte)(lightLevel - 1));
                    propagateSunlightFrom(blockX, blockY - 1, blockZ);
                }
            }
        }
        // Propagate up
        if (blockY < NewChunk.CHUNK_DIMENSION_Y - 1 && worldView.getBlock(blockX, blockY + 1, blockZ).isTranslucent()) {
            byte upperLight = worldView.getSunlight(blockX, blockY + 1, blockZ);
            if (lightLevel == NewChunk.MAX_LIGHT) {
                if (upperLight < lightLevel) {
                    worldView.setSunlight(blockX, blockY + 1, blockZ, lightLevel);
                    propagateSunlightFrom(blockX, blockY + 1, blockZ);
                }
            } else {
                if (upperLight < lightLevel - 1) {
                    worldView.setSunlight(blockX, blockY + 1, blockZ, (byte)(lightLevel - 1));
                    propagateSunlightFrom(blockX, blockY + 1, blockZ);
                }
            }
        }

        if (lightLevel <= 1) return;
        for (Vector3i adjDir : HORIZONTAL_LIGHT_DIRECTIONS) {
            int adjX = blockX + adjDir.x;
            int adjZ = blockZ + adjDir.z;

            byte adjLightValue = worldView.getSunlight(adjX, blockY, adjZ);
            if (adjLightValue < lightLevel - 1 && worldView.getBlock(adjX, blockY, adjZ).isTranslucent()) {
                worldView.setSunlight(adjX, blockY, adjZ, (byte) (lightLevel - 1));
                propagateSunlightFrom(adjX, blockY, adjZ);
            }
        }
    }

    private void propagateLightFrom(int blockX, int blockY, int blockZ) {
        byte lightLevel = worldView.getLight(blockX, blockY, blockZ);
        if (lightLevel <= 1) return;

        for (Vector3i adjDir : HORIZONTAL_LIGHT_DIRECTIONS) {
            int adjX = blockX + adjDir.x;
            int adjZ = blockZ + adjDir.z;

            byte adjLightValue = worldView.getLight(adjX, blockY, adjZ);
            if (adjLightValue < lightLevel - 1 && worldView.getBlock(adjX, blockY, adjZ).isTranslucent()) {
                worldView.setLight(adjX, blockY, adjZ, (byte) (lightLevel - 1));
                propagateLightFrom(adjX, blockY, adjZ);
            }
        }

        if (blockY > 0 && worldView.getBlock(blockX, blockY - 1, blockZ).isTranslucent()) {
            byte lowerLight = worldView.getLight(blockX, blockY - 1, blockZ);
            if (lowerLight < lightLevel - 1) {
                worldView.setLight(blockX, blockY - 1, blockZ, (byte) (lightLevel - 1));
                propagateLightFrom(blockX, blockY - 1, blockZ);
            }
        }
        if (blockY < NewChunk.CHUNK_DIMENSION_Y - 1 && worldView.getBlock(blockX, blockY + 1, blockZ).isTranslucent()) {
            byte upperLight = worldView.getLight(blockX, blockY + 1, blockZ);
            if (upperLight < lightLevel - 1) {
                worldView.setLight(blockX, blockY + 1, blockZ, (byte) (lightLevel - 1));
                propagateLightFrom(blockX, blockY + 1, blockZ);
            }
        }
    }

}
