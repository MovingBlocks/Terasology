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
package com.github.begla.blockmania.utilities;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 3D perlin noise function as shown in the book "Physically Based Rendering".
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PerlinNoise {

    FastRandom rand;
    final int noisePerm[];

    /**
     *
     * @param seed
     */
    public PerlinNoise(int seed) {
        rand = new FastRandom(seed);
        noisePerm = new int[256];

        ArrayList<Integer> permutations = new ArrayList<Integer>();

        for (int i = 0; i < 256; i++) {
            permutations.add(i);
        }

        rand.shuffle(permutations);

        for (int i = 0; i < 256; i++) {
            noisePerm[i] = permutations.get(i).intValue();
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public float noise(float x, float y, float z) {
        // Compute noise cell coordinates and offsets
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y), iz = (int) Math.floor(z);
        float dx = x - ix, dy = y - iy, dz = z - iz;

        // Compute gradient weights
        ix &= (noisePerm.length - 1);
        iy &= (noisePerm.length - 1);
        iz &= (noisePerm.length - 1);

        float w000 = grad(ix, iy, iz, dx, dy, dz);
        float w100 = grad(ix + 1, iy, iz, dx - 1, dy, dz);
        float w010 = grad(ix, iy + 1, iz, dx, dy - 1, dz);
        float w110 = grad(ix + 1, iy + 1, iz, dx - 1, dy - 1, dz);
        float w001 = grad(ix, iy, iz + 1, dx, dy, dz - 1);
        float w101 = grad(ix + 1, iy, iz + 1, dx - 1, dy, dz - 1);
        float w011 = grad(ix, iy + 1, iz + 1, dx, dy - 1, dz - 1);
        float w111 = grad(ix + 1, iy + 1, iz + 1, dx - 1, dy - 1, dz - 1);

        // Compute trilinear interpolation of weights
        float wx = noiseWeight(dx), wy = noiseWeight(dy), wz = noiseWeight(dz);
        float x00 = lerp(wx, w000, w100);
        float x10 = lerp(wx, w010, w110);
        float x01 = lerp(wx, w001, w101);
        float x11 = lerp(wx, w011, w111);
        float y0 = lerp(wy, x00, x10);
        float y1 = lerp(wy, x01, x11);
        return lerp(wz, y0, y1);
    }

    float lerp(float t, float v1, float v2) {
        return (1.f - t) * v1 + t * v2;
    }

    float grad(int x, int y, int z, float dx, float dy, float dz) {
        int h = noisePerm[(noisePerm[(noisePerm[x % noisePerm.length] + y) % noisePerm.length] + z) % noisePerm.length];
        h &= 15;
        float u = h < 8 || h == 12 || h == 13 ? dx : dy;
        float v = h < 4 || h == 12 || h == 13 ? dy : dz;

        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    float noiseWeight(float t) {
        float t3 = t * t * t;
        float t4 = t3 * t;
        return 6.f * t4 * t - 15.f * t4 + 10.f * t3;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param octaves
     * @param lacunarity 
     * @param gain 
     * @return
     */
    public float noiseWithOctaves(float x, float y, float z, int octaves, float lacunarity, float gain) {
        float frequency = 1f;
        float signal = 1f;

        /*
         * Fetch the first noise octave.
         */
        signal = noise(x, y, z);
        float result = signal;
        float weight = 1f;

        for (int i = 0; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            weight = gain * signal;

            if (weight > 1.0f) {
                weight = 1.0f;
            } else if (weight < 0.0f) {
                weight = 0.0f;
            }

            signal = noise(x, y, z);

            signal *= weight;
            result += signal * Math.pow(frequency, -1f);
            frequency *= lacunarity;
        }

        return result;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param octaves
     * @param lacunarity 
     * @param gain 
     * @return
     */
    public float ridgedMultiFractalNoise(float x, float y, float z, int octaves, float lacunarity, float gain) {
        float frequency = 1f;
        float signal = 1f;

        /*
         * Fetch the first noise octave.
         */
        signal = ridge(noise(x, y, z));
        float result = signal;
        float weight = 1f;

        for (int i = 0; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            weight = gain * signal;

            if (weight > 1.0f) {
                weight = 1.0f;
            } else if (weight < 0.0f) {
                weight = 0.0f;
            }

            signal = ridge(noise(x, y, z));

            signal *= weight;
            result += signal * Math.pow(frequency, -1f);
            frequency *= lacunarity;
        }


        return result - 1f;
    }

    private float ridge(float n) {
        n = Math.abs(n);
        n = 1f - n;
        n = n * n;
        return n;
    }
}
