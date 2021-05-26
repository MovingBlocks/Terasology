// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.constraints;

import org.terasology.engine.config.flexible.Setting;

/**
 * Represents constraints on values that can be stored in a {@link Setting}.
 *
 * @param <T> The type of values that are constrained by this {@link SettingConstraint}.
 */
public interface SettingConstraint<T> {
    /**
     * Checks whether the constraint is satisfied by the given value.
     *
     * @param value The value to check.
     * @return True if the value satisfies the constraint, false otherwise.
     */
    boolean isSatisfiedBy(T value);

    /**
     * Logs appropriate warnings assuming that the given value does not satisfy
     * the constraint.
     *
     * @param value The value to issue warnings for.
     */
    void warnUnsatisfiedBy(T value);
}
