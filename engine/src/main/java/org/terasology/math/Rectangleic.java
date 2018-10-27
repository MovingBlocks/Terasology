/*
 * Copyright 2017 MovingBlocks
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

public interface Rectangleic {
    /**
     * The bitmask that indicates that a point lies to the left.
     */
    int OUT_LEFT = 1;

    /**
     * The bitmask that indicates that a point lies above.
     */
    int OUT_TOP = 2;

    /**
     * The bitmask that indicates that a point lies to the right.
     */
    int OUT_RIGHT = 4;

    /**
     * The bitmask that indicates that a point lies below.
     */
    int OUT_BOTTOM = 8;

    int maxX();

    int minX();

    int maxY();

    int minY();

    int width();

    int height();

    boolean isEmpty();

    boolean contains(float x, float y);

    boolean contains(Rectangleic other);

    boolean overlaps(Rectangleic other);

    public int outcode(float x, float y);
}
