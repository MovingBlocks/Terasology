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

import javax.vecmath.Vector2f;

/**
 * A simple helper class for various tasks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Helper {

    private static final double DIV = 1.0 / 16.0;

    /**
     * Calculates the texture offset for a given position within
     * the texture atlas.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The texture offset
     */
    public static Vector2f calcOffsetForTextureAt(int x, int y) {
        return new Vector2f((float) (x * DIV), (float) (y * DIV));
    }

    /**
     * Returns true if the flag at the given byte position
     * is set.
     *
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return True if the flag is set
     */
    public static boolean isFlagSet(byte value, short index) {
        return (value & (1 << index)) != 0;
    }

    /**
     * Sets a flag at a given byte position.
     *
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return The byte value containing the modified flag
     */
    public static byte setFlag(byte value, short index) {
        return (byte) (value | (1 << index));
    }
}
