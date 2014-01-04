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
import org.terasology.utilities.random.FastRandom;

/**
 * Improved Perlin noise based on the reference implementation by Ken Perlin.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PerlinNoise implements Noise3D {

    private final int[] noisePermutations;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public PerlinNoise(int seed) {
        FastRandom rand = new FastRandom(seed);

        noisePermutations = new int[512];
        int[] noiseTable = new int[256];

        // Init. the noise table
        for (int i = 0; i < 256; i++) {
            noiseTable[i] = i;
        }

        // Shuffle the array
        for (int i = 0; i < 256; i++) {
            int j = rand.nextInt(256);

            int swap = noiseTable[i];
            noiseTable[i] = noiseTable[j];
            noiseTable[j] = swap;
        }

        // Finally replicate the noise permutations in the remaining 256 index positions
        for (int i = 0; i < 256; i++) {
            noisePermutations[i] = noiseTable[i];
            noisePermutations[i + 256] = noiseTable[i];
        }

    }

    /**
     * Returns the noise value at the given position.
     *
     * @param posX Position on the x-axis
     * @param posY Position on the y-axis
     * @param posZ Position on the z-axis
     * @return The noise value
     */
    @Override
    public double noise(double posX, double posY, double posZ) {
        int xInt = (int) TeraMath.fastFloor(posX) & 255;
        int yInt = (int) TeraMath.fastFloor(posY) & 255;
        int zInt = (int) TeraMath.fastFloor(posZ) & 255;

        double x = posX - TeraMath.fastFloor(posX);
        double y = posY - TeraMath.fastFloor(posY);
        double z = posZ - TeraMath.fastFloor(posZ);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        int a = noisePermutations[xInt] + yInt;
        int aa = noisePermutations[a] + zInt;
        int ab = noisePermutations[(a + 1)] + zInt;
        int b = noisePermutations[(xInt + 1)] + yInt;
        int ba = noisePermutations[b] + zInt;
        int bb = noisePermutations[(b + 1)] + zInt;

        return lerp(w, lerp(v, lerp(u, grad(noisePermutations[aa], x, y, z),
                grad(noisePermutations[ba], x - 1, y, z)),
                lerp(u, grad(noisePermutations[ab], x, y - 1, z),
                        grad(noisePermutations[bb], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(noisePermutations[(aa + 1)], x, y, z - 1),
                        grad(noisePermutations[(ba + 1)], x - 1, y, z - 1)),
                        lerp(u, grad(noisePermutations[(ab + 1)], x, y - 1, z - 1),
                                grad(noisePermutations[(bb + 1)], x - 1, y - 1, z - 1))));
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

}
