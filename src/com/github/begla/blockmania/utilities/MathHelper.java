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

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MathHelper {

    /**
     * @param x
     * @param y
     * @param q11
     * @param q12
     * @param q21
     * @param q22
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    public static float biLerp(float x, float y, float q11, float q12, float q21, float q22, float x1, float x2, float y1, float y2) {
        float r1 = lerp(x, x1, x2, q11, q21);
        float r2 = lerp(x, x1, x2, q12, q22);
        return lerp(y, y1, y2, r1, r2);
    }

    /*
     * 
     */

    /**
     * @param x
     * @param q00
     * @param q01
     * @param x1
     * @param x2
     * @return
     */
    private static float lerp(float x, float x1, float x2, float q00, float q01) {
        return ((x2 - x) / (x2 - x1)) * q00 + ((x - x1) / (x2 - x1)) * q01;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param q000
     * @param q001
     * @param q010
     * @param q011
     * @param q100
     * @param q101
     * @param q110
     * @param q111
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @return
     */
    public static float triLerp(float x, float y, float z, float q000, float q001, float q010, float q011, float q100, float q101, float q110, float q111, float x1, float x2, float y1, float y2, float z1, float z2) {
        float x00 = lerp(x, x1, x2, q000, q100);
        float x10 = lerp(x, x1, x2, q010, q110);
        float x01 = lerp(x, x1, x2, q001, q101);
        float x11 = lerp(x, x1, x2, q011, q111);
        float r0 = lerp(y, y1, y2, x00, x01);
        float r1 = lerp(y, y1, y2, x10, x11);
        return lerp(z, z1, z2, r0, r1);
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
        int j = (int) Math.floor(Math.sqrt(0.25 + 2 * c) - 0.5);
        return j - cantorY(c);
    }

    /**
     * Inverse function of Cantor's pairing function.
     *
     * @param c Cantor value
     * @return Value along the y-axis
     */
    private static int cantorY(int c) {
        int j = (int) Math.floor(Math.sqrt(0.25 + 2 * c) - 0.5);
        return c - j * (j + 1) / 2;
    }
}
