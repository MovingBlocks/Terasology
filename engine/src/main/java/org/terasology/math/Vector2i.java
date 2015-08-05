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

/**
 * A 2-element vector represented by signed integer x,y
 * coordinates.
 * @deprecated use {@link org.terasology.math.geom.Vector2i} instead.
 * @author Immortius
 */
@Deprecated
public class Vector2i extends org.terasology.math.geom.Vector2i {

    /**
     * Constructs and initializes a Vector2i from the specified
     * x and y coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs and initializes a Vector2i from the specified Vector2i.
     *
     * @param other the Vector2i containing the initialization x and y
     *              data.
     */
    public Vector2i(Vector2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Constructs and initializes a Vector2i to (0,0).
     */
    public Vector2i() {
    }

    /**
     * @return A <b>new</b> instance of (0, 0)
     */
    public static Vector2i zero() {
        return new Vector2i(0, 0);
    }

}

