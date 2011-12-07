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
package com.github.begla.blockmania.noise;

import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.MathHelper;

/**
 * Improved Perlin noise based on the reference implementation by Ken Perlin.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PerlinNoise {

    private final int[] _noisePermutations;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public PerlinNoise(int seed) {
        FastRandom rand = new FastRandom(seed);

        _noisePermutations = new int[512];
        int[] _noiseTable = new int[256];

        // Init. the noise table
        for (int i = 0; i < 256; i++)
            _noiseTable[i] = i;

        // Shuffle the array
        for (int i = 0; i < 256; i++) {
            int j = rand.randomInt() % 256;
            j = (j < 0) ? -j : j;

            int swap = _noiseTable[i];
            _noiseTable[i] = _noiseTable[j];
            _noiseTable[j] = swap;
        }

        // Finally replicate the noise permutations in the remaining 256 index positions
        for (int i = 0; i < 256; i++)
            _noisePermutations[i] = _noisePermutations[i + 256] = _noiseTable[i];

    }

    /**
     * Returns the noise value at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise value
     */
    public double noise(double x, double y, double z) {
        int X = (int) MathHelper.fastFloor(x) & 255, Y = (int) MathHelper.fastFloor(y) & 255, Z = (int) MathHelper.fastFloor(z) & 255;

        x -= MathHelper.fastFloor(x);
        y -= MathHelper.fastFloor(y);
        z -= MathHelper.fastFloor(z);

        double u = fade(x), v = fade(y), w = fade(z);
        int A = _noisePermutations[X] + Y, AA = _noisePermutations[A] + Z, AB = _noisePermutations[(A + 1)] + Z,
                B = _noisePermutations[(X + 1)] + Y, BA = _noisePermutations[B] + Z, BB = _noisePermutations[(B + 1)] + Z;

        return lerp(w, lerp(v, lerp(u, grad(_noisePermutations[AA], x, y, z),
                grad(_noisePermutations[BA], x - 1, y, z)),
                lerp(u, grad(_noisePermutations[AB], x, y - 1, z),
                        grad(_noisePermutations[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(_noisePermutations[(AA + 1)], x, y, z - 1),
                        grad(_noisePermutations[(BA + 1)], x - 1, y, z - 1)),
                        lerp(u, grad(_noisePermutations[(AB + 1)], x, y - 1, z - 1),
                                grad(_noisePermutations[(BB + 1)], x - 1, y - 1, z - 1))));
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x          Position on the x-axis
     * @param y          Position on the y-axis
     * @param z          Position on the z-axis
     * @param octaves    Amount of octaves to apply
     * @param lacunarity The lacunarity
     * @param h          The fractal dimension
     * @return The noise value
     */
    public double fBm(double x, double y, double z, int octaves, double lacunarity, double h) {
        double result = 0.0;

        for (int i = 0; i < octaves; i++) {
            result += noise(x, y, z) * Math.pow(lacunarity, -h * i);

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }

        return result;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
