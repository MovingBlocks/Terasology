/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities.random;

import org.terasology.module.sandbox.API;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;

import java.util.List;

/**
 * Interface for random number generators.
 *
 */
@API
public abstract class Random {

    // This is the list of characters nextString can return
    private static final char[] ALPHANUMERIC_CHARS = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    /**
     * @return Random integer from Integer.MIN_VALUE to Integer.MAX_VALUE
     */
    public abstract int nextInt();

    /**
     * @param max
     * @return Random integer from 0 (inclusive) to max (exclusive)
     */
    public int nextInt(int max) {
        return (int) (max * nextDouble());
    }

    /**
     * @param min
     * @param max
     * @return Random integer from min (inclusive) to max (inclusive)
     */
    public int nextInt(int min, int max) {
        return min + nextInt(max - min + 1);
    }

    /**
     * @return Random long from Long.MIN_VALUE to Long.MAX_VALUE
     */
    public long nextLong() {
        return (long) nextInt() << 32 ^ nextInt();
    }

    /**
     * @param max
     * @return Random long from 0 (inclusive) to max (exclusive)
     */
    public long nextLong(long max) {
        return (long) (max * nextDouble());
    }

    /**
     * @param min
     * @param max
     * @return Random long from min (inclusive) to max (inclusive)
     */
    public long nextLong(long min, long max) {
        return min + nextLong(max - min + 1);
    }

    /**
     * @return Random float from 0 (inclusive) to 1 (exclusive)
     */
    public float nextFloat() {
        return (nextInt() & 0x7FFFFFFF) / (Integer.MAX_VALUE + 1.0f);
    }

    /**
     * @param min
     * @param max
     * @return Random float from min (inclusive) to max (inclusive)
     */
    public float nextFloat(float min, float max) {
        return min + (max - min) * (nextInt() & 0x7FFFFFFF) / Integer.MAX_VALUE;
    }

    /**
     * @return Random double from 0 (inclusive) to 1 (exclusive)
     */
    public double nextDouble() {
        return (nextLong() & 0x7FFFFFFFFFFFFFFFL) / (Long.MAX_VALUE + 1.0);
    }

    /**
     * @param min
     * @param max
     * @return Random double from min (inclusive) to max (inclusive)
     */
    public double nextDouble(double min, double max) {
        return min + (max - min) * (nextLong() & 0x7FFFFFFFFFFFFFFFL) / Long.MAX_VALUE;
    }

    /**
     * @return Random boolean
     */
    public boolean nextBoolean() {
        return nextInt() < 0;
    }

    /**
     * Returns a random alphanumeric string with a certain length
     *
     * @param len String length
     * @return
     */
    public String nextString(int len) {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = ALPHANUMERIC_CHARS[nextInt(ALPHANUMERIC_CHARS.length)];
        }
        return new String(chars);
    }

    /**
     * Returns a random item from the given list, or null is the list is empty
     *
     * @param list
     * @return
     */
    public <T> T nextItem(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * Returns a Vector3f whose components range from -1.0 (inclusive) to 1.0 (inclusive)
     *
     * @return The vector
     */
    public Vector3f nextVector3f() {
        return nextVector3f(new Vector3f());
    }

    /**
     * Returns a Vector3f whose components range from min (inclusive) to max (inclusive)
     *
     * @param min
     * @param max
     * @return The vector
     */
    public Vector3f nextVector3f(float min, float max) {
        return new Vector3f(nextFloat(min, max), nextFloat(min, max), nextFloat(min, max));
    }

    /**
     * Randomises a provided Vector3f so its components range from -1.0 (inclusive) to 1.0 (inclusive)
     *
     * @param output
     * @return
     */
    public Vector3f nextVector3f(Vector3f output) {
        return nextVector3f(-1.0f, 1.0f, output);
    }

    /**
     * Randomises a provided Vector3f so its components range from min (inclusive) to max
     *
     * @param min
     * @param max
     * @param output
     * @return
     */
    public Vector3f nextVector3f(float min, float max, Vector3f output) {
        output.set(nextFloat(min, max), nextFloat(min, max), nextFloat(min, max));
        return output;
    }

    /**
     * Returns a Vector3f with a given size whose components can range from -size (inclusive) to +size (inclusive)
     *
     * @param size
     * @return The vector
     */
    public Vector3f nextVector3f(float size) {
        // Create a vector whose length is not zero
        Vector3f vector = new Vector3f();
        do {
            nextVector3f(vector);
        } while (vector.x == 0.0f && vector.y == 0.0f && vector.z == 0.0f);
        float length = vector.length();
        vector.scale(size / length);
        return vector;
    }

    /**
     * Returns a unit vector (length = 1) Vector3f whose components range from -1 (inclusive) to 1 (inclusive)
     *
     * @return The vector
     */
    public Vector3f nextUnitVector3f() {
        return nextVector3f(1.0f);
    }

    /**
     * Calculates a standardized normal distributed value (using the polar method).
     *
     * @return The value
     */
    public double nextGaussian() {

        double q = Double.MAX_VALUE;
        double u1 = 0;
        double u2;

        while (q >= 1d || q == 0) {
            u1 = nextDouble(-1.0, 1.0);
            u2 = nextDouble(-1.0, 1.0);

            q = TeraMath.pow(u1, 2) + TeraMath.pow(u2, 2);
        }

        double p = Math.sqrt(-2d * Math.log(q) / q);
        return u1 * p; // or u2 * p
    }

    /**
     * Calculates a normal distributed value (using the polar method).
     *
     * <code>nextGuassian(1,1)</code> is equivalent to {@link #nextGaussian()}.
     *
     * @param mean the mean value of the distribution
     * @param stdDev the standard deviation of the distribution
     *
     * @return The value
     */
    public double nextGaussian(double mean, double stdDev) {
        return mean + stdDev * nextGaussian();
    }
}
