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
 *
 */
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.Chunk;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.PerlinNoise;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Generators are used to generate the terrain of chunk, to genreate caves
 * and to populate the surface.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ChunkGenerator {

    /**
     *
     */
    protected PerlinNoise _pGen1;
    /**
     *
     */
    protected PerlinNoise _pGen2;
    /**
     *
     */
    protected PerlinNoise _pGen3;
    /**
     *
     */
    protected final FastRandom _rand;

    /**
     *
     * @param seed
     */
    public ChunkGenerator(String seed) {
        _rand = new FastRandom(seed.hashCode());
        _pGen1 = new PerlinNoise(_rand.randomInt());
        _pGen2 = new PerlinNoise(_rand.randomInt());
        _pGen3 = new PerlinNoise(_rand.randomInt());
    }

    /**
     * 
     * @param c
     */
    public void generate(Chunk c) {
        throw new NotImplementedException();
    }
}
