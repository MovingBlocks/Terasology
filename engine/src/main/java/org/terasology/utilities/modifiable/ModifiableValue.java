// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.utilities.modifiable;

/**
 * A helper type to get and modify the value of a component without changing its actual value
 * <p>
 * The result value is guaranteed to be greater or equal to zero. Components using this type must mention so in their
 * javadoc.
 * </p>
 */
public class ModifiableValue {
    private final float baseValue;

    private float preModifiers;
    private float multipliers;
    private float postModifiers;

    public ModifiableValue(float baseValue) {
        preModifiers = 0f;
        multipliers = 1f;
        postModifiers = 1f;
        this.baseValue = baseValue;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public ModifiableValue multiply(float amount) {
        multipliers += amount;
        return this;
    }

    public ModifiableValue preAdd(float amount) {
        preModifiers += amount;
        return this;
    }

    public ModifiableValue postAdd(float amount) {
        postModifiers += amount;
        return this;
    }

    /**
     * Calculates the result value from the base value and given modifiers and multipliers.
     * <p>
     * The value is calculated based on the following formula:
     * <pre>
     * result = max(0, <baseValue> + Σ <modifier> * Π <multiplier> + Σ <postModifier>)
     * </pre>
     *
     * <emph>The result value is guaranteed to be non-negative!</emph>
     */
    public float getValue() {
        return (baseValue + preModifiers) * multipliers + postModifiers;
    }
}
