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
 * @deprecated Prefer using {@link SimplexNoise}, it is comparable to Perlin noise (fewer directional artifacts, lower computational overhead for higher dimensions).
 *
 */
@Deprecated
public class PerlinNoise extends AbstractNoise implements Noise2D, Noise3D {

    private final int[] noisePermutations;
    private final int permCount;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public PerlinNoise(long seed) {
        this(seed, 1 << 8);
    }

    /**
     * Init. a new generator with a given seed value and grid dimension.
     * Supports tileable noise generation
     *
     * @param seed The seed value
     * @param gridDim gridDim x gridDim will be the size of the perlin's grid of vectors, noise will be tiled if an input coordinate crosses a multiple of gridDim
     */
    public PerlinNoise(long seed, int gridDim) {
        FastRandom rand = new FastRandom(seed);

        permCount = gridDim;
        noisePermutations = new int[permCount * 2];
        int[] noiseTable = new int[permCount];

        // Init. the noise table
        for (int i = 0; i < permCount; i++) {
            noiseTable[i] = i;
        }

        // Shuffle the array
        for (int i = 0; i < permCount; i++) {
            int j = rand.nextInt(permCount);

            int swap = noiseTable[i];
            noiseTable[i] = noiseTable[j];
            noiseTable[j] = swap;
        }

        // Finally replicate the noise permutations in the remaining permCount index positions
        for (int i = 0; i < permCount; i++) {
            noisePermutations[i] = noiseTable[i];
            noisePermutations[i + permCount] = noiseTable[i];
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
    public float noise(float posX, float posY, float posZ) {
        int xInt = Math.floorMod((int) TeraMath.fastFloor(posX), permCount);
        int yInt = Math.floorMod((int) TeraMath.fastFloor(posY), permCount);
        int zInt = Math.floorMod((int) TeraMath.fastFloor(posZ), permCount);

        float x = posX - TeraMath.fastFloor(posX);
        float y = posY - TeraMath.fastFloor(posY);
        float z = posZ - TeraMath.fastFloor(posZ);

        float u = TeraMath.fadePerlin(x);
        float v = TeraMath.fadePerlin(y);
        float w = TeraMath.fadePerlin(z);
        int a = noisePermutations[xInt] + yInt;
        int aa = noisePermutations[a] + zInt;
        int ab = noisePermutations[(a + 1)] + zInt;
        int b = noisePermutations[(xInt + 1)] + yInt;
        int ba = noisePermutations[b] + zInt;
        int bb = noisePermutations[(b + 1)] + zInt;

        float gradAA = grad(noisePermutations[aa], x, y, z);
        float gradBA = grad(noisePermutations[ba], x - 1, y, z);

        float gradAB = grad(noisePermutations[ab], x, y - 1, z);
        float gradBB = grad(noisePermutations[bb], x - 1, y - 1, z);

        float val1 = TeraMath.lerp(TeraMath.lerp(gradAA, gradBA, u), TeraMath.lerp(gradAB, gradBB, u), v);

        float gradAA1 = grad(noisePermutations[(aa + 1)], x, y, z - 1);
        float gradBA1 = grad(noisePermutations[(ba + 1)], x - 1, y, z - 1);

        float gradAB1 = grad(noisePermutations[(ab + 1)], x, y - 1, z - 1);
        float gradBB1 = grad(noisePermutations[(bb + 1)], x - 1, y - 1, z - 1);

        float val2 = TeraMath.lerp(TeraMath.lerp(gradAA1, gradBA1, u), TeraMath.lerp(gradAB1, gradBB1, u), v);

        return TeraMath.lerp(val1, val2, w);
    }

    private static float grad(int hash, float x, float y, float z) {
        int h = hash & 15;
        float u = h < 8 ? x : y;
        float v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

}
