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

import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.World;

/**
 * Object generators are used to generate objects like trees etc.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ObjectGenerator {

    /**
     *
     */
    final FastRandom _rand;
    /**
     *
     */
    final World _world;

    /**
     * @param w
     * @param seed
     */
    ObjectGenerator(World w, String seed) {
        _rand = new FastRandom(seed.hashCode());
        _world = w;
    }

    /**
     * Generates an object at the given position.
     *
     * @param posX   Position on the x-axis
     * @param posY   Position on the y-axis
     * @param posZ   Position on the z-axis
     * @param update If true, the chunk will be queued for updating
     */
    public abstract void generate(int posX, int posY, int posZ, boolean update);
}
