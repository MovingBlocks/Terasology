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
package org.terasology.utilities;

import org.terasology.logic.world.Chunk;

/**
 * Collection of math functions.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MathHelper {

    /**
     * Returns the absolute value.
     *
     * @param i
     * @return
     */
    public static int fastAbs(int i) {
        return (i >= 0) ? i : -i;
    }

    /**
     * Returns the absolute value.
     *
     * @param d
     * @return
     */
    public static double fastAbs(double d) {
        return (d >= 0) ? d : -d;
    }

    public static double fastFloor(double d) {
        int i = (int) d;
        return (d < 0 && d != i) ? i - 1 : i;
    }

    /**
     * Clamps a given value to be an element of [0..1].
     */
    public static double clamp(double value) {
        if (value > 1.0)
            return 1.0;
        if (value < 0.0)
            return 0.0;
        return value;
    }

    /**
     * Bilinear interpolation.
     */
    public static double biLerp(double x, double y, double q11, double q12, double q21, double q22, double x1, double x2, double y1, double y2) {
        double r1 = lerp(x, x1, x2, q11, q21);
        double r2 = lerp(x, x1, x2, q12, q22);
        return lerp(y, y1, y2, r1, r2);
    }

    /**
     * Linear interpolation.
     */
    private static double lerp(double x, double x1, double x2, double q00, double q01) {
        return ((x2 - x) / (x2 - x1)) * q00 + ((x - x1) / (x2 - x1)) * q01;
    }

    /**
     * Trilinear interpolation.
     */
    public static double triLerp(double x, double y, double z, double q000, double q001, double q010, double q011, double q100, double q101, double q110, double q111, double x1, double x2, double y1, double y2, double z1, double z2) {
        double x00 = lerp(x, x1, x2, q000, q100);
        double x10 = lerp(x, x1, x2, q010, q110);
        double x01 = lerp(x, x1, x2, q001, q101);
        double x11 = lerp(x, x1, x2, q011, q111);
        double r0 = lerp(y, y1, y2, x00, x01);
        double r1 = lerp(y, y1, y2, x10, x11);
        return lerp(z, z1, z2, r0, r1);
    }

    /**
     * Maps any given value to be positive only.
     */
    public static int mapToPositive(int x) {
        if (x >= 0)
            return x * 2;

        return -x * 2 - 1;
    }

    /**
     * Recreates the original value after applying "mapToPositive".
     */
    public static int redoMapToPositive(int x) {
        if (x % 2 == 0) {
            return x / 2;
        }

        return -(x / 2) - 1;
    }

    /**
     * Applies Cantor's pairing function to 2D coordinates.
     *
     * @param k1 X-coordinate
     * @param k2 Y-coordinate
     * @return Unique 1D value
     */
    public static int cantorize(int k1, int k2) {
        return ((k1 + k2) * (k1 + k2 + 1) / 2) + k2;
    }

    /**
     * Inverse function of Cantor's pairing function.
     *
     * @param c Cantor value
     * @return Value along the x-axis
     */
    public static int cantorX(int c) {
        int j = (int) (Math.sqrt(0.25 + 2 * c) - 0.5);
        return j - cantorY(c);
    }

    /**
     * Inverse function of Cantor's pairing function.
     *
     * @param c Cantor value
     * @return Value along the y-axis
     */
    public static int cantorY(int c) {
        int j = (int) (Math.sqrt(0.25 + 2 * c) - 0.5);
        return c - j * (j + 1) / 2;
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param x The X-coordinate of the block
     * @return The X-coordinate of the chunk
     */
    public static int calcChunkPosX(int x) {
        // Offset for chunks with negative positions
        if (x < 0)
            x -= 15;

        return (x / Chunk.CHUNK_DIMENSION_X);
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param z The Z-coordinate of the block
     * @return The Z-coordinate of the chunk
     */
    public static int calcChunkPosZ(int z) {
        // Offset for chunks with negative positions
        if (z < 0)
            z -= 15;

        return (z / Chunk.CHUNK_DIMENSION_Z);
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param x1 The X-coordinate of the block in the world
     * @param x2 The X-coordinate of the chunk in the world
     * @return The X-coordinate of the block within the chunk
     */
    public static int calcBlockPosX(int x1, int x2) {
        return MathHelper.fastAbs(x1 - (x2 * Chunk.CHUNK_DIMENSION_X));
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param z1 The Z-coordinate of the block in the world
     * @param z2 The Z-coordinate of the chunk in the world
     * @return The Z-coordinate of the block within the chunk
     */
    public static int calcBlockPosZ(int z1, int z2) {
        return MathHelper.fastAbs(z1 - (z2 * Chunk.CHUNK_DIMENSION_Z));
    }
}
