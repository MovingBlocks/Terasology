/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.utilities.procedural;

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
