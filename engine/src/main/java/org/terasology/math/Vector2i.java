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

package org.terasology.math;

import javax.vecmath.Tuple2i;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;

/**
 * A 2-element vector represented by signed integer x,y
 * coordinates.
 *
 * @author Immortius
 */
public class Vector2i extends Tuple2i {

    private static final long serialVersionUID = 3862467945178721785L;

    /**
     * Constructs and initializes a Vector2i from the specified
     * x and y coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Vector2i(int x, int y) {
        super(x, y);
    }

    /**
     * Constructs and initializes a Vector2i from the array of length 2.
     *
     * @param values the array of length 2 containing x and y in order.
     */
    public Vector2i(int[] values) {
        super(values);
    }

    /**
     * Constructs and initializes a Vector2i from the specified Vector2i.
     *
     * @param other the Vector2i containing the initialization x and y
     *           data.
     */
    public Vector2i(Vector2i other) {
        super(other);
    }

    /**
     * Constructs and initializes a Vector2i to (0,0).
     */
    public Vector2i() {
        super();
    }

    /**
     * @return The equivalent Vector2f
     */
    public Vector2f toVector2f() {
        return new Vector2f(x, y);
    }

    /**
     * @return The equivalent Vector2d
     */
    public Vector2d toVector2d() {
        return new Vector2d(x, y);
    }

    /**
     * @return A <b>new</b> instance of (0, 0)
     */
    public static Vector2i zero() {
        return new Vector2i(0, 0);
    }
}
