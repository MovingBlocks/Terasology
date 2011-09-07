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
     * @param seed
     */
    public PerlinNoise(int seed) {
        FastRandom rand = new FastRandom(seed);
        _noisePermutations = new int[512];

        for (int i = 0; i < 256; i++) {
            int r = rand.randomInt();

            if (r < 0)
                r *= -1;

            _noisePermutations[i] = _noisePermutations[i + 256] = r % 256;
        }
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double noise(double x, double y, double z) {
        int X = (int) MathHelper.fastFloor(x) & 255, Y = (int) MathHelper.fastFloor(y) & 255, Z = (int) MathHelper.fastFloor(z) & 255;

        x -= MathHelper.fastFloor(x);
        y -= MathHelper.fastFloor(y);
        z -= MathHelper.fastFloor(z);

        double u = fade(x), v = fade(y), w = fade(z);
        int A = _noisePermutations[X % 255] + Y, AA = _noisePermutations[A % 255] + Z, AB = _noisePermutations[(A + 1) % 255] + Z,
                B = _noisePermutations[(X + 1) % 255] + Y, BA = _noisePermutations[B % 255] + Z, BB = _noisePermutations[(B + 1) % 255] + Z;

        return lerp(w, lerp(v, lerp(u, grad(_noisePermutations[AA % 255], x, y, z),
                grad(_noisePermutations[BA % 255], x - 1, y, z)),
                lerp(u, grad(_noisePermutations[AB % 255], x, y - 1, z),
                        grad(_noisePermutations[BB % 255], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(_noisePermutations[(AA + 1) % 255], x, y, z - 1),
                        grad(_noisePermutations[(BA + 1) % 255], x - 1, y, z - 1)),
                        lerp(u, grad(_noisePermutations[(AB + 1) % 255], x, y - 1, z - 1),
                                grad(_noisePermutations[(BB + 1) % 255], x - 1, y - 1, z - 1))));
    }

    /**
     * @param t
     * @return
     */
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * @param t
     * @param a
     * @param b
     * @return
     */
    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    /**
     * @param hash
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param octaves
     * @param lacunarity
     * @return
     */
    public double multiFractalNoise(double x, double y, double z, int octaves, double lacunarity) {
        double result = 0;

        for (int i = 1; i <= octaves; i++) {
            result += noise(x, y, z) * Math.pow(lacunarity, -0.96471 * i);

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }

        return result;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param octaves
     * @param lacunarity
     * @param gain
     * @param offset
     * @return
     */
    public double ridgedMultiFractalNoise(double x, double y, double z, int octaves, double lacunarity, double gain, double offset) {
        double frequency = 1f;
        double signal;

        /*
         * Fetch the first noise octave.
         */
        signal = ridge(noise(x, y, z), offset);
        double result = signal;
        double weight;

        for (int i = 1; i <= octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            weight = gain * signal;

            if (weight > 1.0f) {
                weight = 1.0f;
            } else if (weight < 0.0f) {
                weight = 0.0f;
            }

            signal = ridge(noise(x, y, z), offset);

            signal *= weight;
            result += signal * Math.pow(frequency, -0.96461f);
            frequency *= lacunarity;
        }


        return result;
    }

    private double ridge(double n, double offset) {
        n = MathHelper.fastAbs(n);
        n = offset - n;
        n = n * n;
        return n;
    }
}
