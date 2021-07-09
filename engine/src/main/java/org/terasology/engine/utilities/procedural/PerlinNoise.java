// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.procedural;

import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.math.TeraMath;

/**
 * Domain-rotated Perlin noise. Based on reference implementation by Ken Perlin, with domain rotation by K.jpg
 *
 * Perlin noise is an older form of noise designed without isotropy in mind. It produces significant square-aligned
 * directional artifacts, when evaluated on planes aligned to its internal coordinate grid. However, when used for
 * 2D-focused use-cases of 3D such as flat-world voxel terrain with overhangs, its results can be greatly improved
 * by using a rotated coordinate space. Here in this implementation, Y is rotated to point up the main diagonal of
 * the noise grid, while X and Z span the planes perpendicular to that. Square bias is effectively hidden, and
 * visible directional bias is greatly reduced.
 *
 * It is worthwhile to note that this technique results in different input coordinates being differently useful
 * for different purposes. When using the noise in 2D-based 3D voxel world, Y should be the vertical direction.
 * For a 2D animation, Y should be the time variable. To generate 2D-only noise, use X and Z only, with Y fixed.
 * This enables best utilization of the orientation of the noise grid.
 *
 * Note that this technique requires the 3D noise to produce nice 2D results.
 */
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
     *
     * @param seed The seed value
     * @param permCount The size of the permutation table.
     */
    public PerlinNoise(long seed, int permCount) {
        FastRandom rand = new FastRandom(seed);

        this.permCount = permCount;
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
     * Returns the domain-rotated noise value at the given position.
     * If generating noise with a clearly defined vertical direction, assign that to Y. {@code noise(horz0, vert, horz1)}
     * If generating noise to animate a 2D plane, use Y as your time variable. {@code noise(horz0, time, horz1)}
     * If generating 2D-only noise, use X and Z, with Y set to a constant value. {@code noise(horz0, const, horz1)}
     * Y should always be the "different" direction in whatever your use case is.
     * The way the noise changes along Y is slightly different from its behavior in X/Z.
     *
     * @param posX Position on the x-axis (horizontal)
     * @param posY Position on the y-axis (vertical, time, or fixed)
     * @param posZ Position on the z-axis (horizontal)
     * @return The noise value
     */
    @Override
    public float noise(float posX, float posY, float posZ) {

        // Domain rotation removes Perlin's characteristic square artifacts from the XZ planes, by pointing Y up the grid's main diagonal.
        // Ordinarily, X can be said to move in the unit vector direction <1, 0, 0>, Y in <0, 1, 0>, and Z in <0, 0, 1>. With this rotation,
        // moving along the input for Y now moves in the unit direction <0.577, 0.577, 0.577> in the noise's internal coordinate space.
        // Perpendicular to that, X and Z move in the directions <0.789, -0.577, -0.211> and <-0.211, -0.577, 0.789>. These vectors form a
        // rotation matrix. The code is a simplification of the multiplication of this rotation matrix by the input coordinate, taking
        // advantage of the many repetitions of 0.577 and the fact that 0.789 = 1-0.211.
        float xz = posX + posZ;
        float s2 = xz * -0.211324865405187f;
        float yy = posY * 0.577350269189626f;
        float rPosX = posX + (s2 + yy);
        float rPosY = xz * -0.577350269189626f + yy;
        float rPosZ = posZ + (s2 + yy);

        int xInt = Math.floorMod(TeraMath.floorToInt(rPosX), permCount);
        int yInt = Math.floorMod(TeraMath.floorToInt(rPosY), permCount);
        int zInt = Math.floorMod(TeraMath.floorToInt(rPosZ), permCount);

        float x = rPosX - TeraMath.fastFloor(rPosX);
        float y = rPosY - TeraMath.fastFloor(rPosY);
        float z = rPosZ - TeraMath.fastFloor(rPosZ);

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
