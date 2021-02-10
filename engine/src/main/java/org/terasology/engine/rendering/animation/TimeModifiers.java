/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.animation;

import com.google.common.base.Preconditions;
import org.terasology.math.TeraMath;

/**
 * A collection of {@link TimeModifier} implementations
 */
public final class TimeModifiers {

    private TimeModifiers() {
        // no instances
    }

    /**
     * Always returns the same constant value
     * @param constant the constant value
     * @return a mapping function
     */
    public static TimeModifier constant(float constant) {
        Preconditions.checkArgument(constant >= 0 && constant <= 1);
        return v -> constant;
    }

    /**
     * Always returns the value without transformation
     * @return the identity function
     */
    public static TimeModifier linear() {
        return v -> v;
    }

    /**
     * Always the square of the the given value. Useful for constant accelerations.
     * @return a mapping function
     */
    public static TimeModifier square() {
        return v -> v * v;
    }

    /**
     * Inverts the value, i.e returns one minus <code>v</code>
     * @return a mapping function
     */
    public static TimeModifier inverse() {
        return v -> (1 - v);
    }

    /**
     * Linear increase until v=0.5 and the returned value equals one and decreasing linearly until v=1.0.
     * @return a mapping function
     */
    public static TimeModifier mirror() {
        return v -> (v < 0.5f) ? v * 2f : (1 - v) * 2;
    }

    /**
     * Applies a constant offset to given value. Wraps around.
     * @param delta the offset
     * @return a mapping function
     */
    public static TimeModifier rotate(float delta) {
        return v -> (v + delta) % 1f;
    }

    /**
     * increases the speed by factor <code>times</codes> and restarts from 0.
     * @param times the (positive) multiplication factor
     * @return a mapping function
     */
    public static TimeModifier multiply(float times) {
        Preconditions.checkArgument(times > 0f);
        return v -> (v * times) % 1f;
    }

    /**
     * Maps to a sub-region of [0..1]
     * @param min the lower bound
     * @param max the upper bound
     * @return a transformation from [0..1] to [min..max]
     */
    public static TimeModifier sub(float min, float max) {
        Preconditions.checkArgument(min >= 0f);
        Preconditions.checkArgument(max > min);
        Preconditions.checkArgument(max <= 1f);

        float range = max - min;
        return v -> min + (v * range);
    }

    /**
     * Smooth start, fast in the middle, smooth end. Almost identical to sin^2, but faster
     * @return a mapping function
     */
    public static TimeModifier smooth() {
        return v -> TeraMath.fadeHermite(v);
    }
}
