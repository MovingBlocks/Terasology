// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.utilities.modifiable;

import org.terasology.module.sandbox.API;

/**
 * A helper type to get and modify the value of a component without changing its actual value.
 * <p>
 * Components using this type must mention so in their javadoc so all modifiers are added correctly.
 * </p>
 */
@API
public class ModifiableValue {
    private final float baseValue;

    private float preModifiers;
    private float multipliers;
    private float postModifiers;

    public ModifiableValue(float baseValue) {
        preModifiers=0;
        multipliers=1;
        postModifiers=0;
        this.baseValue=baseValue;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public ModifiableValue multiply(float amount) {
        multipliers *= amount;
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
     * result = (<baseValue> + Σ <modifier>) * Π <multiplier> + Σ <postModifier>
     * </pre>
     *
     * <emph>non-negativity of the value is not ensured and must be checked by the system if needed</emph>
     */
    public float getValue() {
        return (baseValue + preModifiers) * multipliers + postModifiers;
    }

    public float getPreModifiers() {
        return preModifiers;
    }

    public float getPostModifiers() {
        return postModifiers;
    }

    public float getMultipliers() {
        return multipliers;
    }

    public void setPreModifiers(float preModifiers){
        this.preModifiers=preModifiers;
    }

    public void setMultipliers(float multipliers){
        this.multipliers=multipliers;
    }

    public void setPostModifiers(float postModifiers){
        this.postModifiers=postModifiers;
    }
}
