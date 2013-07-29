/*
 * Copyright 2013 Moving Blocks
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
 * Improved Perlin noise based on the reference implementation by Ken Perlin.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PerlinNoise implements Noise {

    private static final double LACUNARITY = 2.1379201;
    private static final double H = 0.836281;

    private double[] spectralWeights;

    private final int[] noisePermutations;
    private boolean recomputeSpectralWeights = true;
    private int octaves = 9;

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
            int j = rand.randomInt() % 256;
            j = (j < 0) ? -j : j;

            int swap = noiseTable[i];
            noiseTable[i] = noiseTable[j];
            noiseTable[j] = swap;
        }

        // Finally replicate the noise permutations in the remaining 256 index positions
        for (int i = 0; i < 256; i++) {
            noisePermutations[i] = noisePermutations[i + 256] = noiseTable[i];
        }

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
        int xInt = (int) TeraMath.fastFloor(x) & 255;
        int yInt = (int) TeraMath.fastFloor(y) & 255;
        int zInt = (int) TeraMath.fastFloor(z) & 255;

        x -= TeraMath.fastFloor(x);
        y -= TeraMath.fastFloor(y);
        z -= TeraMath.fastFloor(z);

        double u = fade(x), v = fade(y), w = fade(z);
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

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise value
     */
    public double fBm(double x, double y, double z) {
        double result = 0.0;

        if (recomputeSpectralWeights) {
            spectralWeights = new double[octaves];

            for (int i = 0; i < octaves; i++) {
                spectralWeights[i] = java.lang.Math.pow(LACUNARITY, -H * i);
            }

            recomputeSpectralWeights = false;
        }

        for (int i = 0; i < octaves; i++) {
            result += noise(x, y, z) * spectralWeights[i];

            x *= LACUNARITY;
            y *= LACUNARITY;
            z *= LACUNARITY;
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

    public void setOctaves(int octaves) {
        this.octaves = octaves;
        recomputeSpectralWeights = true;
    }

    public int getOctaves() {
        return octaves;
    }
}
