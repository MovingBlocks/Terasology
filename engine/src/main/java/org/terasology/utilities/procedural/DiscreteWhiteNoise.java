// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.procedural;

import org.terasology.math.TeraMath;

/**
 * This implementation is based on Robert Jenkins' 96 bit mix function as described in
 * in "Integer Hash Function" by Thomas Wang, Jan 1997. The original code is public domain.
 * <br><br>
 * This implementation rounds float parameters to the closest integer value. Use it in combination
 * with BrownianNoise at lacunarity = 2.0.
 */
public class DiscreteWhiteNoise extends WhiteNoise {

    /**
     * Initializes a new instance of the random number generator using a
     * specified seed.
     *
     * @param seed The seed to use
     */
    public DiscreteWhiteNoise(int seed) {
        super(seed);
    }

    @Override
    public float noise(float x, float y, float z) {
        int fx = TeraMath.floorToInt(x + 0.5f);
        int fy = TeraMath.floorToInt(y + 0.5f);
        int fz = TeraMath.floorToInt(z + 0.5f);
        return noise(fx, fy, fz);
    }

    @Override
    public float noise(float x, float y) {
        int fx = TeraMath.floorToInt(x + 0.5f);
        int fy = TeraMath.floorToInt(y + 0.5f);
        return noise(fx, fy);
    }
}
