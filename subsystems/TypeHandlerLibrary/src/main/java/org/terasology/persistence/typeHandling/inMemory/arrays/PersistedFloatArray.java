// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedFloat;

import java.util.Iterator;

public class PersistedFloatArray extends PersistedNumberArray {

    private final TFloatList data;

    public PersistedFloatArray(TFloatList data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public TDoubleList getAsDoubleArray() {
        return TroveUtils.floatToDouble(data);
    }

    @Override
    public TFloatList getAsFloatArray() {
        return data;
    }

    @Override
    public TIntList getAsIntegerArray() {
        return TroveUtils.floatToInt(data);
    }

    @Override
    public TLongList getAsLongArray() {
        return TroveUtils.floatToLong(data);
    }

    @Override
    public PersistedData getArrayItem(int index) {
        return new PersistedFloat(data.get(index));
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public Iterator<PersistedData> iterator() {
        return TroveUtils.iteratorFrom(data);
    }
}
