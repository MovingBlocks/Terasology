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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a {@link Number} within the specified range.
 *
 * @param <T> The type of the {@link Number} to validate.
 */
public class RangedNumberValidator<T extends Number & Comparable<? super T>> implements SettingValueValidator<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RangedNumberValidator.class);
    private boolean minInclusive;
    private boolean maxInclusive;

    private T min;
    private T max;

    public RangedNumberValidator(T min, T max, boolean minInclusive, boolean maxInclusive) {
        this.min = min;
        this.max = max;

        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public boolean isMinInclusive() {
        return minInclusive;
    }

    public boolean isMaxInclusive() {
        return maxInclusive;
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
        boolean withinMinBoundary = true;

        if (min != null) {
            if (minInclusive) {
                withinMinBoundary = value.compareTo(min) >= 0;
            } else {
                withinMinBoundary = value.compareTo(min) > 0;
            }
        }

        boolean withinMaxBoundary = true;

        if (max != null) {
            if (maxInclusive) {
                withinMaxBoundary = value.compareTo(max) <= 0;
            } else {
                withinMaxBoundary = value.compareTo(max) < 0;
            }
        }

        return withinMinBoundary && withinMaxBoundary;
    }

    @Override
    public void issueWarnings(T value) {
        LOGGER.warn("Value {} is not in the range {}{}, {}{}", value, minInclusive ? "[" : "(",
                min != null ? min : "UNBOUNDED", max != null ? max : "UNBOUNDED", maxInclusive ? "]" : ")");
    }
}
