/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.entitySystem.event;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

/**
 * A generic event for getting a value for a property.
 * <p>
 * The result value is guaranteed to be greater or equal to zero.
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
     * Calculates the result value from the base value and given modifiers and multipliers.
     * <p>
     * The value is calculated based on the following formula:
     * <pre>
     * result = max(0, (<baseValue> + Σ <modifier>) * Π <multiplier> + Σ <postModifier>)
     * </pre>
     *
     * <emph>The result value is guaranteed to be non-negative!</emph>
     */
    public float getResultValue() {
        //TODO(skaldarnar): Based on a discussion in https://github.com/MovingBlocks/Terasology/pull/4063 we may want
        // to lift the guarantee/restriction that the result value needs to be non-negative. Systems are still free to
        // apply this restriction if needed.
        return Math.max(0, (baseValue + modifiers.sum()) * product(multipliers) + postModifiers.sum());
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
