// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedBoolean;

import java.util.Arrays;
import java.util.Iterator;

// TODO, see https://github.com/MovingBlocks/Terasology/issues/5176 for reasoning.
@SuppressWarnings({"PMD.ArrayIsStoredDirectly", "PMD.MethodReturnsInternalArray"})
public class PersistedBooleanArray extends AbstractPersistedArray {

    private final boolean[] booleans;

    public PersistedBooleanArray(boolean[] booleans) {
        this.booleans = booleans;
    }


    @Override
    public int size() {
        return booleans.length;
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return new PersistedBoolean(booleans[index]);
    }

    @Override
    public boolean isBooleanArray() {
        return true;
    }

    @Override
    public boolean[] getAsBooleanArray() {
        return booleans;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return new Iterator<PersistedData>() {
            final boolean[] bools = Arrays.copyOf(booleans, booleans.length);
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < bools.length;
            }

            @Override
            public PersistedData next() {
                return new PersistedBoolean(bools[index++]);
            }
        };
    }
}
