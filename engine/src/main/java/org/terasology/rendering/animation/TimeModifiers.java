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

import java.util.Objects;
import java.util.function.Function;

import org.terasology.math.TeraMath;

import com.google.api.client.util.Preconditions;

/**
 * A collection of {@link TimeModifier} implementations
 */
public final class TimeModifiers {

    public static TimeModifier constant(float constant) {
        Preconditions.checkArgument(constant >= 0 && constant <= 1);
        return v -> constant;
    }

    public static TimeModifier linear() {
        return v -> v;
    }

    public static TimeModifier square() {
        return v -> v * v;
    }

    public static TimeModifier inverse() {
        return v -> (1 - v);
    }

    public static TimeModifier mirror(float delta) {
        return v -> (v < 0.5f) ? v * 2f : (1 - v) * 2;
    }

    public static TimeModifier rotate(float delta) {
        return v -> (v + delta) % 1f;
    }

    public static TimeModifier multiply(float times) {
        return v -> (v * times) % 1f;
    }

    /**
     * Smooth start, fast in the middle, smooth end. Almost identical to sin^2, but faster
     */
    public static TimeModifier smooth() {
        return v -> TeraMath.fadeHermite(v);
    }
}
