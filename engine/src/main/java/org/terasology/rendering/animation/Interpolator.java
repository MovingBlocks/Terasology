/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.animation;

/*
 */
public interface Interpolator {
    /**
     * Returns where an interpolated value should be based on
     * where the position an animation is in.
     *
     * @param v position of the animation between the start and end [0:1]
     * and also referenced as the intermediate interpolation <b>v</b>alue
     *
     * @return where the interpolated value should be, between [start:end]
     * if setStart or setEnd have been called, else [0:1]
     */
    float getInterpolation(float v);

    /**
     * Sets the begining value of the interpolation.
     *
     * @param v the begining interpolation value
     */
    void setStart(float v);

    /**
     * Returns the begining interpolation value
     *
     * @return the begining interpolation value
     */
    float getStart();

    /**
     * Sets the ending value of the interpolation.
     *
     * @param v the ending interpolation value
     */
    void setEnd(float v);

    /**
     * Returns the ending interpolation value.
     *
     * @return the ending interpolation value
     */
    float getEnd();
}
