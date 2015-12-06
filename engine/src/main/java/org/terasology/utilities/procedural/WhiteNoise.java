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

/**
 * This implementation is based on Robert Jenkins' 96 bit mix function as described in
 * in "Integer Hash Function" by Thomas Wang, Jan 1997. The original code is public domain.
 *
 */
public class WhiteNoise implements Noise {

    private final int seed;

    /**
     * Initializes a new instance of the random number generator using a
     * specified seed.
     *
     * @param seed The seed to use
     */
    public WhiteNoise(long seed) {
        this.seed = (int) (seed ^ 780291637); // flip some bits
    }

    @Override
    public float noise(float x, float y, float z) {
        // the floatToIntBits conversion messes with the coords
        // this is ok, because white noise is invariant to position
        int fx = Float.floatToIntBits(x);
        int fy = Float.floatToIntBits(y);
        int fz = Float.floatToIntBits(z);
        return noise(fx, fy, fz);
    }

    @Override
    public float noise(float x, float y) {
        // the floatToIntBits conversion messes with the coords
        // this is ok, because white noise is invariant to position
        int fx = Float.floatToIntBits(x);
        int fy = Float.floatToIntBits(y);
        return noise(fx, fy);
    }

    @Override
    public float noise(int x, int y) {
        return noise(intNoise(x) + y);
    }

    @Override
    public float noise(int x, int y, int z) {
        return noise(intNoise(intNoise(x) + y) + z);
    }

    /**
     * @param x any float value
     * @return a random, but deterministic float value in [-1..1]
     */
    public float noise(float x) {
        // the floatToIntBits conversion messes with the coords
        // this is ok, because white noise is invariant to position
        int fx = Float.floatToIntBits(x);
        return noise(fx);
    }

    /**
     * @param x and integer value
     * @return a random, but deterministic float value in [-1..1]
     */
    public float noise(int x) {
        float noise = intNoise(x);
        return noise / Integer.MAX_VALUE;
    }

    public int intNoise(int x, int y) {
        return intNoise(intNoise(x) + y);
    }

    public int intNoise(int x, int y, int z) {
        return intNoise(intNoise(intNoise(x) + y) + z);
    }

    /**
     * @param x any integer value
     * @return the 32-bit Jenkins hash
     */
    public int intNoise(int x) {
        int key = seed ^ x;
        int c2 = 0x27d4eb2d; // a prime or an odd constant
        key = (key ^ 61) ^ (key >>> 16);
        key = key + (key << 3);
        key = key ^ (key >>> 4);
        key = key * c2;
        key = key ^ (key >>> 15);
        return key;
    }
}
