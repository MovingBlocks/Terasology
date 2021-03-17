// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.animation;

import java.util.Objects;

/**
 * Applies a functional transformation to a given float value.
 */
public interface TimeModifier {
    /**
     * Modifies a given (temporal) value
     * @param value a value in the range [0..1]
     * @return a value in the range [0..1]
     */
    float apply(float value);

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default TimeModifier andThen(TimeModifier after) {
        Objects.requireNonNull(after);
        return (t) -> after.apply(apply(t));
    }
}
