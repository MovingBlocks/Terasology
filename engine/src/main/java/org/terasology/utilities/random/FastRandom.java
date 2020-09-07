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
package org.terasology.utilities.random;

import org.terasology.gestalt.module.sandbox.API;

/**
 * Random number generator based on the Xorshift generator by George Marsaglia.
 *
 */
@API
public class FastRandom extends Random {

    private long seed = System.currentTimeMillis();

    /**
     * Initializes a new instance of the random number generator using
     * a specified seed.
     *
     * @param seed The seed to use
     */
    public FastRandom(long seed) {
        this.seed = seed;
    }

    /**
     * Initializes a new instance of the random number generator using
     * "System.currentTimeMillis()" as seed.
     */
    public FastRandom() {
    }

    /**
     * Returns a random int value.
     *
     * @return Random value
     */
    @Override
    public int nextInt() {
        seed++;
        seed ^= (seed << 21);
        seed ^= (seed >>> 35);
        seed ^= (seed << 4);
        return (int) seed;
    }
}
