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

package org.terasology.logic.newWorld.generator;

import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.NewChunkGenerator;
import org.terasology.logic.newWorld.WorldBiomeProvider;
import org.terasology.logic.newWorld.WorldProvider;
import org.terasology.math.Vector3i;

/**
 * Propagates internal lights and sunlight of a chunk
 * @author Immortius
 */
public class InternalLightGenerator implements NewChunkGenerator{

    private static final Vector3i[] HORIZONTAL_LIGHT_DIRECTIONS = {
            new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
            new Vector3i(0, 0, 1), new Vector3i(0, 0, -1)
    };

    private static final Vector3i[] ALL_LIGHT_DIRECTIONS = {
            new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
            new Vector3i(0, 1, 0), new Vector3i(0, -1, 0),
            new Vector3i(0, 0, 1), new Vector3i(0, 0, -1)
    };

    @Override
    public void setWorldSeed(String seed) {
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
    }

    @Override
    public void generateChunk(NewChunk chunk) {
        int top = NewChunk.CHUNK_DIMENSION_Y - 1;

        // Tunnel light down
        for (int x = 0; x < NewChunk.CHUNK_DIMENSION_X; x++) {
            for (int z = 0; z < NewChunk.CHUNK_DIMENSION_Z; z++) {
                for (int y = top; y >= 0; y--) {
                    if (chunk.getBlock(x, y, z).isTranslucent()) {
                        chunk.setSunlight(x, y, z, NewChunk.MAX_LIGHT);
                    } else {
                        break;
                    }
                }
            }
        }

        for (int x = 0; x < NewChunk.CHUNK_DIMENSION_X; x++) {
            for (int z = 0; z < NewChunk.CHUNK_DIMENSION_Z; z++) {
                for (int y = top; y >= 0; y--) {
                    if (chunk.getBlock(x, y, z).isTranslucent()) {
                        spreadSunlightInternal(chunk, x,y,z);
                        spreadLightInternal(chunk,x,y,z);
                    }
                }
            }
        }
    }

    @Override
    public void postProcessChunk(Vector3i pos, WorldProvider world) {
    }

    private void spreadLightInternal(NewChunk chunk, int x, int y, int z) {
        byte lightValue = chunk.getLight(x,y,z);
        if (lightValue == 0) return;

        // TODO: use custom bounds checked iterator for this
        for (Vector3i adjDir : ALL_LIGHT_DIRECTIONS) {
            int adjX = x + adjDir.x;
            int adjY = y + adjDir.y;
            int adjZ = z + adjDir.z;
            if (chunk.isInBounds(adjX, adjY, adjZ)) {
                byte adjLightValue = chunk.getLight(adjX,adjY,adjZ);

                if (adjLightValue < lightValue - 1 && chunk.getBlock(adjX, y, adjZ).isTranslucent()) {
                    chunk.setLight(adjX, adjY, adjZ, (byte)(lightValue - 1));
                    spreadLightInternal(chunk,adjX, adjY, adjZ);
                }
            }
        }
    }

    private void spreadSunlightInternal(NewChunk chunk, int x, int y, int z) {
        byte lightValue = chunk.getSunlight(x,y,z);
        if (lightValue == 0) return;

        for (Vector3i adjDir : HORIZONTAL_LIGHT_DIRECTIONS) {
            int adjX = x + adjDir.x;
            int adjZ = z + adjDir.z;

            if (chunk.isInBounds(adjX, y, adjZ)) {
                byte adjLightValue = chunk.getSunlight(adjX,y,adjZ);
                if (adjLightValue < lightValue - 1 && chunk.getBlock(adjX, y, adjZ).isTranslucent()) {
                    chunk.setSunlight(adjX, y, adjZ, (byte)(lightValue - 1));
                    spreadSunlightInternal(chunk, adjX, y, adjZ);
                }
            }
        }

        // If it was max it would already have been spread down
        if (y > 0 && lightValue < NewChunk.MAX_LIGHT && chunk.getSunlight(x, y - 1, z) < lightValue - 1 && chunk.getBlock(x, y-1, z).isTranslucent()) {
            chunk.setSunlight(x, y - 1, z, (byte)(lightValue - 1));
            spreadSunlightInternal(chunk, x, y - 1, z);
        }
    }
}
