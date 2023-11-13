// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.event;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * A generic event for getting a value for a property.
 * <p>
 * There are 2 different methods which calculate the Result Value for this event:
 *  1. getResultValue() - The result value is guaranteed to be greater or equal to zero.
 *  2. getResultValueWithoutCapping() - Negative result values are allowed in this method.
 */
public abstract class AbstractValueModifiableEvent implements Event {
    private final float baseValue;

    private TFloatList modifiers = new TFloatArrayList();
    private TFloatList multipliers = new TFloatArrayList();
    private TFloatList postModifiers = new TFloatArrayList();

    protected AbstractValueModifiableEvent(float baseValue) {
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

    /**
     * @deprecated please use {@link #postAdd(float)} instead.
     */
    @Deprecated
    public void addPostMultiply(float amount) {
        postAdd(amount);
    }

    public void postAdd(float amount) {
        postModifiers.add(amount);
    }

    /**
     * Uses {@link AbstractValueModifiableEvent#getResultValueWithoutCapping()} to obtain the result value and returns
     * the same if positive and zero if negative.
     * <pre>
     * result = max(0, ResultValueWithoutCapping)
     * </pre>
     *
     * <em>The result value is guaranteed to be non-negative!</em>
     */
    public float getResultValue() {
        //TODO(skaldarnar): Based on a discussion in https://github.com/MovingBlocks/Terasology/pull/4063 we may want
        // to lift the guarantee/restriction that the result value needs to be non-negative. Systems are still free to
        // apply this restriction if needed.
        return Math.max(0, getResultValueWithoutCapping());
    }

    /**
     * This is a temporary method to be used in events where negative value support is essential for calculating the
     * result value.
     * <p>
     * Calculates the result value from the base value and given modifiers and multipliers.
     * <p>
     * The value is calculated based on the following formula:
     * <pre> {@code
     *      result = (<baseValue> + Σ <modifier>) * Π <multiplier> + Σ <postModifier>
     * } </pre>
     *
     * <em>Negative result values are allowed here.</em>
     */
    public float getResultValueWithoutCapping() {
        //TODO: Based on an extended discussion from : https://github.com/MovingBlocks/Terasology/pull/4063
        // This is a temporary method which should be merged with the getResultValue() after all its uses are
        // checked out and corrected for(if needed).
        return (baseValue + modifiers.sum()) * product(multipliers) + postModifiers.sum();
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
