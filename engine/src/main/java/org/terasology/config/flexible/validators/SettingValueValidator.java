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
package org.terasology.config.flexible.validators;

/**
 * Validates the given value and issues warnings if the value is invalid.
 * @param <T> The type of values this {@link SettingValueValidator} validates.
 */
public interface SettingValueValidator<T> {
    /**
     * Checks whether the given value is valid or not.
     * @param value The value to validate.
     * @return True if the value is valid, false otherwise.
     */
    boolean fastValidate(T value);

    /**
     * Issues (logs) appropriate warnings assuming that the given value is invalid.
     * @param value The value to issue warnings for.
     */
    void issueWarnings(T value);

    /**
     * Checks whether the given value is valid or not and issues appropriate warnings if it is invalid.
     * @param value The value to validate.
     * @return True if the value is valid, false otherwise.
     */
    default boolean validate(T value) {
        boolean isValid = fastValidate(value);

        if (!isValid) {
            issueWarnings(value);
        }

        return isValid;
    }
}
