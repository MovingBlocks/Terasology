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

import com.github.begla.blockmania.world.World;
import com.github.begla.blockmania.utilities.FastRandom;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Object generators are used to generate objects within the world.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ObjectGenerator {

    /**
     *
     */
    protected final FastRandom _rand;
    /**
     *
     */
    protected final World _world;

    /**
     *
     * @param w
     * @param seed
     */
    public ObjectGenerator(World w, String seed) {
        _rand = new FastRandom(seed.hashCode());
        _world = w;
    }

    /**
     * Generates an object at the given origin.
     *
     * @param posX Origin on the x-axis
     * @param posY Origin on the y-axis
     * @param posZ Origin on the z-axis
     * @param update If true the chunk will be queued for updating
     */
    public void generate(int posX, int posY, int posZ, boolean update) {
        throw new NotImplementedException();
    }
}
