// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.random;

import org.terasology.context.annotation.API;

@API
public class PDist {
    /**
     * average value
     */
    public float mean;

    /**
     * maximum deviation from the mean
     */
    public float range;

    /**
     * Enumeration of valid distribution types
     */
    public enum Type {
        uniform, normal
    }

    /**
     * distribution type
     */
    public Type type;

    public PDist(float mean, float range, Type type) {
        set(mean, range, type);
    }

    public PDist(float mean, float range) {
        set(mean, range, Type.uniform);
    }

    public PDist() {
        set(0, 0, Type.uniform);
    }

    /**
     * Set distribution parameters
     * @param meanValue
     * @param rangeValue
     * @param typeValue
     * @return
     */
    public PDist set(float meanValue, float rangeValue, Type typeValue) {
        this.mean = meanValue;
        this.range = rangeValue >= 0 ? rangeValue : -rangeValue;
        this.type = typeValue;
        return this;
    }

    /**
     * Calculate maximum possible value
     * @return
     */
    public float getMax() {
        return mean + range;
    }

    /**
     * Calculate maximum possible value
     */
    public float getMin() {
        return mean - range;
    }

    /**
     * Calculate a random floating point value from the distribution.
     *
     * @param rand java.util.Random object, appropriately seeded
     * @return
     */
    public float getValue(Random rand) {
        if (range == 0) {
            return mean;
        }
        switch (type) {
            case uniform:
                return (rand.nextFloat() * 2.0F - 1.0F) * range + mean;
            case normal:
                float value = (float) rand.nextGaussian() / 2.5F; // compress to normal distribution with mean = 0, stddev = 0.4
                if (value < -1.0F) {
                    value = -1.0F; // force value within valid range (clamps most extreme 3.5% of values)
                } else if (value > 1.0F) {
                    value = 1.0F;
                }
                return value * range + mean; // transform into normal distribution with requested mean & stddev = 0.4*range
            default:
                return 0; // should *never* happen
        }
    }

    /**
     * Calculate a random integer value from the distribution.
     * Preserves the mean of the distribution, unlike naively rounding the results of getValue().
     * However, this method can return values outside the specified range, and will increase
     * the standard deviation.
     *
     * @param rand java.util.Random object, appropriately seeded
     * @return
     */
    public int getIntValue(Random rand) {
        float fval = getValue(rand);
        int ival = (int) fval;
        fval -= ival;
        if (fval > 0 && fval > rand.nextFloat()) {
            ival++;
        } else if (fval < 0 && -fval > rand.nextFloat()) {
            ival--;
        }
        return ival;
    }

    @Override
    public String toString() {
        return String.format("%f +- %f %s", mean, range, type.name());
    }
}
