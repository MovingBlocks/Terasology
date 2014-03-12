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
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class AbstractValueModifiableEvent implements Event {
    private float baseValue;

    protected AbstractValueModifiableEvent(float baseValue) {
        this.baseValue = baseValue;
    }

    private TFloatList multipliers = new TFloatArrayList();
    private TFloatList modifiers = new TFloatArrayList();

    public float getBaseValue() {
        return baseValue;
    }

    public void multiply(float amount) {
        this.multipliers.add(amount);
    }

    public void add(float amount) {
        modifiers.add(amount);
    }

    public float getResultValue() {
        // For now, add all modifiers and multiply by all multipliers. Negative modifiers cap to zero, but negative
        // multipliers remain.

        float result = baseValue;
        TFloatIterator modifierIter = modifiers.iterator();
        while (modifierIter.hasNext()) {
            result += modifierIter.next();
        }
        result = Math.max(0, result);
        if (result == 0) {
            return 0;
        }
        TFloatIterator multiplierIter = multipliers.iterator();
        while (multiplierIter.hasNext()) {
            result *= multiplierIter.next();
        }
        return result;
    }
}
