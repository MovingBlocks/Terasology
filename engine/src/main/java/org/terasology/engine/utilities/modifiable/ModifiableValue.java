// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.modifiable;

import org.terasology.context.annotation.API;

/**
 * A helper type to get and modify the value of a component without changing its actual value.
 * <p>
 * Components using this type must mention so in their javadoc so all modifiers are added correctly.
 * </p>
 */
@API
public class ModifiableValue {
    private final float baseValue;

    private float preModifier;
    private float multiplier;
    private float postModifier;

    public ModifiableValue(float baseValue) {
        preModifier = 0;
        multiplier = 1;
        postModifier = 0;
        this.baseValue = baseValue;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public ModifiableValue multiply(float amount) {
        multiplier *= amount;
        return this;
    }

    public ModifiableValue preAdd(float amount) {
        preModifier += amount;
        return this;
    }

    public ModifiableValue postAdd(float amount) {
        postModifier += amount;
        return this;
    }

    /**
     * Calculates the result value from the base value and given modifiers and multiplier.
     * <p>
     * The value is calculated based on the following formula:
     * {@code result = (<baseValue> + Σ <modifier>) * Π <multiplier> + Σ <postModifier>}
     *
     * <em>non-negativity of the value is not ensured and must be checked by the system if needed</em>
     */
    public float getValue() {
        return (baseValue + preModifier) * multiplier + postModifier;
    }

    public float getPreModifier() {
        return preModifier;
    }

    public float getPostModifier() {
        return postModifier;
    }

    public float getMultiplier() {
        return multiplier;
    }

    /**
     * Setter method used to set the preModifier at once.
     * It is only used for setting the modifier during the Deserialization process.
     * For any modification, use the preAdd() method instead.
     * @param preModifier the preModifier to set for the component data.
     */
    public void setPreModifier(float preModifier) {
        this.preModifier = preModifier;
    }

    /**
     * Setter method used to set the multiplier at once.
     * It is only used for setting the multiplier during the Deserialization process.
     * For any modification, use the multiply() method instead.
     * @param multiplier the multiplier to set for the component data.
     */
    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Setter method used to set the postModifier at once.
     * It is only used for setting the postModifier during the Deserialization process.
     * For any modification, use the postAdd() method instead.
     * @param postModifier the postModifier to set for the component data.
     */
    public void setPostModifier(float postModifier) {
        this.postModifier = postModifier;
    }
}
