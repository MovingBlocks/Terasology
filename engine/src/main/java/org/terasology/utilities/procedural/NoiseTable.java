// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.procedural;

import org.terasology.engine.utilities.random.FastRandom;

/**
 * A simple integer noise table. This can be used for semi-random eventually repeating data - good for low level noise like the positioning of plants.
 *
 * It is based off of the noisePermutation table used by Perlin noise.
 * @deprecated use {@link WhiteNoise} or {@link DiscreteWhiteNoise} instead
 */
@Deprecated
public class NoiseTable {

    private final int[] noisePermutations;

    public NoiseTable(long seed) {
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

    public int noise(int x) {
        int xInt = x & 255;
        return noisePermutations[xInt];
    }

    public int noise(int x, int y) {
        int xInt = x & 255;
        int yInt = y & 255;
        return noisePermutations[noisePermutations[xInt] + yInt];
    }

    public int noise(int x, int y, int z) {
        int xInt = x & 255;
        int yInt = y & 255;
        int zInt = z & 255;
        return noisePermutations[noisePermutations[noisePermutations[xInt] + yInt] + zInt];
    }
}
