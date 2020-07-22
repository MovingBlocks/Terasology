// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.utilities.modifiable;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

/**
 * A helper type to get and modify the value of a component without changing its actual value
 * <p>
 * The result value is guaranteed to be greater or equal to zero.
 * Components using this type must mention so in their javadoc.
 * </p>
 */
public class ModifiableValue {
    private final float baseValue;

    private TFloatList modifiers = new TFloatArrayList();
    private TFloatList multipliers = new TFloatArrayList();
    private TFloatList postModifiers = new TFloatArrayList();

    public ModifiableValue(float baseValue) {
        this.baseValue = baseValue;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public void multiply(float amount) {
        this.multipliers.add(amount);
    }

    public void add(float amount) {
        modifiers.add(amount);
    }

    public void postAdd(float amount) {
        postModifiers.add(amount);
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
        return Math.max(0, baseValue + modifiers.sum() * product(multipliers) + postModifiers.sum());
    }

    public TFloatList getModifiers() {
        return modifiers;
    }

    public void setModifiers(TFloatList modifiers) {
        this.modifiers = modifiers;
    }

    public TFloatList getMultipliers() {
        return multipliers;
    }

    public void setMultipliers(TFloatList multipliers) {
        this.multipliers = multipliers;
    }

    public TFloatList getPostModifiers() {
        return postModifiers;
    }

    public void setPostModifiers(TFloatList postModifiers) {
        this.postModifiers = postModifiers;
    }

    /**
     * Calculate the product of all values in the given list.
     * <p>
     * A static helper dual to {@code TFloatList#sum()}.
     *
     * @param multipliers the list of multipliers to calculate the product of.
     * @return the product of all values. 1 if the list is empty.
     */
    private static float product(TFloatList multipliers) {
        float multiplierProduct = 1f;
        TFloatIterator multiplierIter = multipliers.iterator();
        while (multiplierIter.hasNext()) {
            multiplierProduct *= multiplierIter.next();
        }
        return multiplierProduct;
    }

}
