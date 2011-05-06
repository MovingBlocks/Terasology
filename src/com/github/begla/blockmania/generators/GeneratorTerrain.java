/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.Chunk;
import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.World;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.PerlinNoise;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GeneratorTerrain implements Generator {

    protected PerlinNoise _pGen1;
    protected PerlinNoise _pGen2;
    protected PerlinNoise _pGen3;
    protected final FastRandom _rand;

    public GeneratorTerrain(String seed) {
        _rand = new FastRandom(seed.hashCode());
        _pGen1 = new PerlinNoise(_rand.randomInt());
        _pGen2 = new PerlinNoise(_rand.randomInt());
        _pGen3 = new PerlinNoise(_rand.randomInt());
    }

    public void generate(Chunk c, World parent) {
        int xOffset = (int) c.getPosition().x * (int) Configuration.CHUNK_DIMENSIONS.x;
        int yOffset = (int) c.getPosition().y * (int) Configuration.CHUNK_DIMENSIONS.y;
        int zOffset = (int) c.getPosition().z * (int) Configuration.CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcTerrainElevation(x + xOffset, z + zOffset) + (calcTerrainRoughness(x + xOffset, z + zOffset) * calcTerrainDetail(x + xOffset, z + zOffset)) * 64);

                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    if (calcCaveDensityAt(x + xOffset, i + yOffset, z + zOffset) < 0.5) {
                        if (calcCanyonDensity(x + xOffset, i + yOffset, z + zOffset) > 0.1f) {
                            if (i == height) {
                                /*
                                 * Grass covers the terrain.
                                 */
                                if (i > 32) {
                                    c.setBlock(x, i, z, 0x1);
                                } else if (i <= 34 && i >= 28) {
                                    c.setBlock(x, i, z, 0x7);
                                } else {
                                    c.setBlock(x, i, z, 0x2);
                                }
                            } else if (i < height) {
                                if (i < height * 0.75f) {

                                    /*
                                     * Generate stone within the terrain
                                     */
                                    c.setBlock(x, i, z, 0x3);
                                } else {
                                    /*
                                     * The upper layer is filled with dirt.
                                     */
                                    if (i <= 34 && i >= 28) {
                                        c.setBlock(x, i, z, 0x7);
                                    } else {
                                        c.setBlock(x, i, z, 0x2);
                                    }
                                }

                                if (i <= 34 && i >= 28) {
                                    /**
                                     * Generate beach.
                                     */
                                    c.setBlock(x, i, z, 0x7);
                                }
                            }
                        }
                    }

                    if (i <= 30 && i > 0) {
                        if (c.getBlock(x, i, z) == 0) {
                            c.setBlock(x, i, z, 0x4);
                        }
                    }

                    if (i == 0) {
                        c.setBlock(x, i, z, 0x8);
                    }
                }
            }
        }
    }

    /**
     * Returns the base elevation for the terrain.
     */
    protected float calcTerrainElevation(float x, float z) {
        float result = 0.0f;
        result += _pGen1.noise(0.003f * x, 0.003f, 0.003f * z) * 128f;
        return Math.abs(result);
    }

    /**
     * Returns the roughness for the base terrain.
     */
    protected float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += _pGen1.noise(0.04f * x, 0.04f, 0.04f * z);
        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     */
    protected float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += _pGen2.noise(0.02f * x, 0.02f, 0.02f * z);
        return result;
    }

    /**
     * Returns the canyon density for the base terrain.
     */
    protected float calcCanyonDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen3.noise(0.01f * x, 0.01f * y, 0.01f * z);
        return (float) Math.abs(result);
    }

    /**
     * Returns the cave density for the base terrain.
     */
    protected float calcCaveDensityAt(float x, float y, float z) {
        float result = 0.0f;
        result += _pGen3.noise(0.06f * x, 0.06f * y, 0.06f * z);
        return result;
    }
}
