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

import org.joml.Vector2fc;
import org.joml.Vector2ic;

/**
 * Interface for all kinds of 2D shapes
 * @author Martin Steiger
 */
public interface Shape {

    /**
     * The exact definition of <i>insideness</i> depends on the implementation
     * @param v the position coordinates
     * @return true if the polygon contains the point
     */
    boolean contains(Vector2fc v);

    /**
     * The exact definition of <i>insideness</i> depends on the implementation
     * @param v the position coordinates
     * @return true if the polygon contains the point
     */
    boolean contains(Vector2ic v);

    /**
     * The exact definition of <i>insideness</i> depends on the implementation
     * @param x the x coord
     * @param y the y coord
     * @return true if the polygon contains the point
     */
    boolean contains(float x, float y);

    /**
     * @return the bounding box of the shape
     */
    Rect2f getBounds();
}
