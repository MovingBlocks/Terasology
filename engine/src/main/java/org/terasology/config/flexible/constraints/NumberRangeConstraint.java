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
package org.terasology.config.flexible.constraints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.flexible.Setting;

/**
 * Constrains a {@link Number} within the specified range in a {@link Setting}.
 *
 * @param <T> The type of {@link Number} to constrain.
 */
public class NumberRangeConstraint<T extends Number & Comparable<? super T>> implements SettingConstraint<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NumberRangeConstraint.class);
    private boolean minInclusive;
    private boolean maxInclusive;

    private T min;
    private T max;

    /**
     * Creates a new instance of {@link NumberRangeConstraint}.
     * 
     * @param min The minimum value in the range. A null value signifies the absence of any minimum value.
     * @param max The maximum value in the range. A null value signifies the absence of any maximum value.
     * @param minInclusive Should the minimum value be included in the range?
     * @param maxInclusive Should the maximum value be included in the range?
     */
    public NumberRangeConstraint(T min, T max, boolean minInclusive, boolean maxInclusive) {
        this.min = min;
        this.max = max;

        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    /**
     * Returns a boolean stating whether the minimum value should be included in the range.
     */
    public boolean isMinInclusive() {
        return minInclusive;
    }

    /**
     * Returns a boolean stating whether the maximum value should be included in the range.
     */
    public boolean isMaxInclusive() {
        return maxInclusive;
    }

    /**
     * Returns the minimum value in the range. A null value signifies the absence of any minimum value.
     */
    public T getMin() {
        return min;
    }

    /**
     * Returns the maximum value in the range. A null value signifies the absence of any maximum value.
     */
    public T getMax() {
        return max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSatisfiedBy(T value) {
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void warnUnsatisfiedBy(T value) {
        LOGGER.warn("Value {} is not in the range {}{}, {}{}", value, minInclusive ? "[" : "(",
                min != null ? min : "UNBOUNDED", max != null ? max : "UNBOUNDED", maxInclusive ? "]" : ")");
    }
}
