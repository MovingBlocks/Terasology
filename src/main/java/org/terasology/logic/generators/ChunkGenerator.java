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
package org.terasology.logic.generators;

import org.terasology.logic.world.Chunk;
import org.terasology.utilities.PerlinNoise;

/**
 * Generators are used to generate the terrain, to generate caves and to populate the surface.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ChunkGenerator {

    protected final PerlinNoise _pGen1, _pGen2, _pGen3, _pGen4, _pGen5, _pGen6, _pGen7, _pGen8;
    protected final GeneratorManager _parent;

    /**
     * Init. the generator with a given seed value.
     *
     * @param generatorManager The generator manager
     */
    public ChunkGenerator(GeneratorManager generatorManager) {
        _pGen1 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode());
        _pGen1.setOctaves(8);

        _pGen2 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 1);
        _pGen2.setOctaves(8);

        _pGen3 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 2);
        _pGen3.setOctaves(8);

        _pGen4 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 3);
        _pGen5 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 4);
        _pGen6 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 5);
        _pGen7 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 6);
        _pGen8 = new PerlinNoise(generatorManager.getParent().getSeed().hashCode() + 7);

        _parent = generatorManager;
    }

    /**
     * Apply the generation process to the given chunk.
     *
     * @param c The chunk to generate/populate
     */
    public abstract void generate(Chunk c);
}
