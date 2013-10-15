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
package org.terasology.utilities.procedural;

import org.terasology.math.TeraMath;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * Random number generator based on the Xorshift generator by George Marsaglia.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FastRandom {

    private static final char[] VALID_CHARS = new char[] {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

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
     * Returns a random long value.
     *
     * @return Random value
     */
    public long randomLong() {
        seed++;
        seed ^= (seed << 21);
        seed ^= (seed >>> 35);
        seed ^= (seed << 4);
        return seed;
    }

    /**
     * Returns a random int value.
     *
     * @return Random value
     */
    public int randomInt() {
        return (int) randomLong();
    }

    public int randomInt(int range) {
        return (int) randomLong() % range;
    }

    public int randomIntAbs() {
        return TeraMath.fastAbs(randomInt());
    }

    public int randomIntAbs(int range) {
        return TeraMath.fastAbs(randomInt() % range);
    }

    public <T> T randomItem(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(randomIntAbs(list.size()));
    }

    /**
     * Returns a random double value.
     *
     * @return Random value between -1.0 and 1.0
     */
    public double randomDouble() {
        return randomLong() / ((double) Long.MAX_VALUE - 1d);
    }

    /**
     * @return Random value between -1f and 1f
     */
    public float randomFloat() {
        return randomLong() / ((float) Long.MAX_VALUE - 1f);
    }

    /**
     * @return A random vector3f with each value between -1f and 1f
     */
    //TODO: Produce a unit vector
    public Vector3f randomVector3f() {
        return new Vector3f(randomFloat(), randomFloat(), randomFloat());
    }

    /**
     * @return A random vector3f with each value between -1f and 1f
     */
    //TODO: Produce a unit vector
    public Vector3f randomVector3f(float scale) {
        Vector3f vec = new Vector3f(randomFloat(), randomFloat(), randomFloat());
        vec.normalize();
        vec.scale(scale);
        return vec;
    }

    /**
     * @return Random value between 0f and 1f
     */
    public float randomPosFloat() {
        return 0.5f * (randomFloat() + 1.0f);
    }

    /**
     * Returns a random bool.
     *
     * @return Random value
     */
    public boolean randomBoolean() {
        return randomLong() > 0;
    }

    /**
     * Returns a random character string with a specified length.
     *
     * @param length The length of the generated string
     * @return Random character string
     */
    public String randomCharacterString(int length) {
        char[] randomChars = new char[length];
        for (int i = 0; i < length; i++) {
            randomChars[i] = VALID_CHARS[VALID_CHARS.length * TeraMath.fastAbs(randomDouble())];
        }
        return new String(randomChars);
    }

    /**
     * Calculates a standardized normal distributed value (using the polar method).
     *
     * @return The value
     */
    public double standNormalDistrDouble() {

        double q = Double.MAX_VALUE;
        double u1 = 0;
        double u2;

        while (q >= 1d || q == 0) {
            u1 = randomDouble();
            u2 = randomDouble();

            q = TeraMath.pow(u1, 2) + TeraMath.pow(u2, 2);
        }

        double p = java.lang.Math.sqrt((-2d * (java.lang.Math.log(q))) / q);
        return u1 * p; // or u2 * p
    }
}
