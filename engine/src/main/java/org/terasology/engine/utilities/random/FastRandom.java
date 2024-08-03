// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.random;

import org.terasology.context.annotation.API;

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
