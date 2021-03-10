/*
 * Copyright 2019 MovingBlocks
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
