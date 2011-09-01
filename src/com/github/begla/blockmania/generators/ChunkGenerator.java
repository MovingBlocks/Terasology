/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.PerlinNoise;
import com.github.begla.blockmania.utilities.VoronoiNoise;
import com.github.begla.blockmania.world.Chunk;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Generators are used to generate the basic terrain, to generate caves
 * and to populate the surface.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ChunkGenerator {

    /**
     * First Perlin noise generator.
     */
    final PerlinNoise _pGen1;
    /**
     * Second Perlin noise generator.
     */
    final PerlinNoise _pGen2;
    /**
     * Third Perlin noise generator.
     */
    final PerlinNoise _pGen3;
    /**
     * Fast random number generator.
     */
    final FastRandom _rand;
    /**
     * TODO
     */
    final VoronoiNoise _voronoi;

    /**
     * Init. the generator with a given seed value.
     *
     * @param seed
     */
    ChunkGenerator(String seed) {
        _rand = new FastRandom(seed.hashCode());
        _pGen1 = new PerlinNoise(seed.hashCode());
        _pGen2 = new PerlinNoise(seed.hashCode() + 1);
        _pGen3 = new PerlinNoise(seed.hashCode() + 2);
        _voronoi = new VoronoiNoise((seed.hashCode()));
    }

    /**
     * Apply the generation process to the given chunk.
     *
     * @param c
     */
    public void generate(Chunk c) {
        throw new NotImplementedException();
    }

    /**
     * @param c
     * @return
     */
    int getOffsetX(Chunk c) {
        return (int) c.getPosition().x * (int) Configuration.CHUNK_DIMENSIONS.x;
    }

    /**
     * @param c
     * @return
     */
    int getOffsetY(Chunk c) {
        return (int) c.getPosition().y * (int) Configuration.CHUNK_DIMENSIONS.y;
    }

    /**
     * @param c
     * @return
     */
    int getOffsetZ(Chunk c) {
        return (int) c.getPosition().z * (int) Configuration.CHUNK_DIMENSIONS.z;
    }
}
