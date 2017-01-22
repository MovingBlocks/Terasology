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
 * Validates a {@link Number} within the specified range.
 * @param <T> The type of the {@link Number} to validate.
 */
public class RangedNumberValidator<T extends Number & Comparable<? super T>> implements SettingValueValidator<T> {
    private boolean inclusive;
    private T min;
    private T max;

    public RangedNumberValidator(T min, T max) {
        this(min, max, false);
    }

    public RangedNumberValidator(T min, T max, boolean inclusive) {
        this.min = min;
        this.max = max;
        this.inclusive = inclusive;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(T value) {
        int lowComp = min != null ? min.compareTo(value) : -1;
        int highComp = max != null ? max.compareTo(value) : 1;

        boolean isValid = lowComp < 0 && highComp > 0;

        if(inclusive)
            isValid = isValid || lowComp == 0 || highComp == 0;

        return isValid;
    }
}
